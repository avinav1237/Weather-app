package com.example.metroweatherapp.domain.usecase

import com.example.metroweatherapp.domain.model.City
import com.example.metroweatherapp.domain.repository.DomainResult
import com.example.metroweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(query: String): DomainResult<List<City>> {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return DomainResult.Success(emptyList())
        }
        return weatherRepository.searchCities(trimmed)
    }
}
