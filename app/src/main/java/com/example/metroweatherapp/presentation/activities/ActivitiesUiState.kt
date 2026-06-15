package com.example.metroweatherapp.presentation.activities

import com.example.metroweatherapp.domain.model.AppError
import com.example.metroweatherapp.domain.model.City
import com.example.metroweatherapp.domain.model.RankedActivity

data class ActivitiesUiState(
    val searchQuery: String = "",
    val citySuggestions: List<City> = emptyList(),
    val isSearching: Boolean = false,
    val selectedCity: City? = null,
    val recommendations: List<RankedActivity> = emptyList(),
    val isLoadingForecast: Boolean = false,
    val error: AppError? = null,
)
