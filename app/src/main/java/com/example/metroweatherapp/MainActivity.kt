package com.example.metroweatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metroweatherapp.presentation.activities.ActivitiesScreen
import com.example.metroweatherapp.presentation.activities.ActivitiesViewModel
import com.example.metroweatherapp.ui.theme.MetroWeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ActivitiesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetroWeatherAppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                ActivitiesScreen(
                    uiState = uiState,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onCitySelected = viewModel::onCitySelected,
                    onRetry = viewModel::onRetry,
                    onDismissError = viewModel::onDismissError,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
