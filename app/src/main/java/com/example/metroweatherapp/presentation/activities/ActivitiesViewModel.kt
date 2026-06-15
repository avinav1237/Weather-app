package com.example.metroweatherapp.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.metroweatherapp.domain.model.City
import com.example.metroweatherapp.domain.repository.DomainResult
import com.example.metroweatherapp.domain.usecase.GetActivityRecommendationsUseCase
import com.example.metroweatherapp.domain.usecase.SearchCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val getActivityRecommendationsUseCase: GetActivityRecommendationsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        searchQueryFlow
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { query -> performCitySearch(query) }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                error = null,
                selectedCity = if (query != it.selectedCity?.name) null else it.selectedCity,
                recommendations = if (query != it.selectedCity?.name) emptyList() else it.recommendations,
            )
        }
        searchQueryFlow.value = query
    }

    fun onCitySelected(city: City) {
        _uiState.update {
            it.copy(
                searchQuery = city.name,
                selectedCity = city,
                citySuggestions = emptyList(),
                isSearching = false,
                error = null,
                recommendations = emptyList(),
            )
        }
        searchQueryFlow.value = city.name
        loadRecommendations(city)
    }

    fun onRetry() {
        val state = _uiState.value
        val city = state.selectedCity
        if (city != null) {
            loadRecommendations(city)
        } else {
            performCitySearch(state.searchQuery)
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun performCitySearch(query: String) {
        if (_uiState.value.selectedCity?.name == query) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            when (val result = searchCitiesUseCase(query)) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            citySuggestions = result.value,
                            isSearching = false,
                        )
                    }
                }
                is DomainResult.Error -> {
                    _uiState.update {
                        it.copy(
                            citySuggestions = emptyList(),
                            isSearching = false,
                            error = result.error,
                        )
                    }
                }
            }
        }
    }

    private fun loadRecommendations(city: City) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingForecast = true,
                    error = null,
                    recommendations = emptyList(),
                )
            }
            when (val result = getActivityRecommendationsUseCase(city)) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            recommendations = result.value,
                            isLoadingForecast = false,
                        )
                    }
                }
                is DomainResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingForecast = false,
                            error = result.error,
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
