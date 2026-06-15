package com.example.metroweatherapp.domain.repository

import com.example.metroweatherapp.domain.model.AppError
import com.example.metroweatherapp.domain.model.City
import com.example.metroweatherapp.domain.model.DailyForecast

sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Error(val error: AppError) : DomainResult<Nothing>
}

interface WeatherRepository {
    suspend fun searchCities(query: String): DomainResult<List<City>>
    suspend fun getDailyForecast(latitude: Double, longitude: Double): DomainResult<List<DailyForecast>>
}
