package com.example.metroweatherapp.presentation.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.metroweatherapp.domain.model.AppError
import com.example.metroweatherapp.presentation.components.CitySuggestionRow
import com.example.metroweatherapp.presentation.components.RankedActivityCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    uiState: ActivitiesUiState,
    onSearchQueryChanged: (String) -> Unit,
    onCitySelected: (com.example.metroweatherapp.domain.model.City) -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        val error = uiState.error ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = error.toUserMessage(),
            actionLabel = "Retry",
        )
        if (result == SnackbarResult.ActionPerformed) {
            onRetry()
        } else {
            onDismissError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Activity Recommendations") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search city") },
                singleLine = true,
            )

            if (uiState.isSearching) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.citySuggestions.isNotEmpty() && uiState.selectedCity == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(uiState.citySuggestions, key = { it.id }) { city ->
                        CitySuggestionRow(
                            city = city,
                            onClick = { onCitySelected(city) },
                        )
                    }
                }
            }

            uiState.selectedCity?.let { city ->
                Text(
                    text = "Selected: ${city.displayName}",
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            if (uiState.isLoadingForecast) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.recommendations.isNotEmpty()) {
                Text(
                    text = "Best activities for the next 7 days",
                    style = MaterialTheme.typography.titleMedium,
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(uiState.recommendations, key = { _, item -> item.activity.name }) { index, item ->
                        RankedActivityCard(
                            rank = index + 1,
                            rankedActivity = item,
                        )
                    }
                }
            } else if (
                uiState.selectedCity != null &&
                !uiState.isLoadingForecast &&
                uiState.error == null
            ) {
                Text(
                    text = "No recommendations available.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else if (
                uiState.searchQuery.length >= 2 &&
                uiState.citySuggestions.isEmpty() &&
                !uiState.isSearching &&
                uiState.selectedCity == null &&
                uiState.error == null
            ) {
                Text(
                    text = "No cities found.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun AppError.toUserMessage(): String {
    return when (this) {
        is AppError.Network -> message
        is AppError.Unknown -> message ?: "Something went wrong. Please try again."
    }
}
