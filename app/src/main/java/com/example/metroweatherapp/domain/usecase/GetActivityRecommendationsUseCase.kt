package com.example.metroweatherapp.domain.usecase

import com.example.metroweatherapp.domain.model.City
import com.example.metroweatherapp.domain.model.RankedActivity
import com.example.metroweatherapp.domain.recommendation.ActivityRecommendationEngine
import com.example.metroweatherapp.domain.repository.DomainResult
import com.example.metroweatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class GetActivityRecommendationsUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val recommendationEngine: ActivityRecommendationEngine,
) {
    suspend operator fun invoke(city: City): DomainResult<List<RankedActivity>> {
        return when (val forecastResult = weatherRepository.getDailyForecast(city.latitude, city.longitude)) {
            is DomainResult.Success -> DomainResult.Success(recommendationEngine.rankActivities(forecastResult.value))
            is DomainResult.Error -> forecastResult
        }
    }
}
