package com.example.metroweatherapp.presentation.activities

import com.example.metroweatherapp.domain.model.AppError
import com.example.metroweatherapp.domain.model.City
import com.example.metroweatherapp.domain.model.DailyForecast
import com.example.metroweatherapp.domain.recommendation.ActivityRecommendationEngine
import com.example.metroweatherapp.domain.repository.DomainResult
import com.example.metroweatherapp.domain.repository.WeatherRepository
import com.example.metroweatherapp.domain.usecase.GetActivityRecommendationsUseCase
import com.example.metroweatherapp.domain.usecase.SearchCitiesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `onSearchQueryChanged updates search query`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSearchQueryChanged("Paris")

        assertEquals("Paris", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `onSearchQueryChanged clears selected city when query changes`() = runTest {
        val viewModel = createViewModel()
        viewModel.onCitySelected(london)

        viewModel.onSearchQueryChanged("Lon")

        assertNull(viewModel.uiState.value.selectedCity)
    }

    @Test
    fun `onDismissError clears error`() = runTest {
        val viewModel = createViewModel(
            forecastResult = DomainResult.Error(AppError.Network()),
        )
        viewModel.onCitySelected(london)

        viewModel.onDismissError()

        assertNull(viewModel.uiState.value.error)
    }

    private fun createViewModel(
        searchResult: DomainResult<List<City>> = DomainResult.Success(emptyList()),
        forecastResult: DomainResult<List<DailyForecast>> = DomainResult.Success(emptyList()),
    ): ActivitiesViewModel {
        val repository = FakeWeatherRepository(
            searchResult = searchResult,
            forecastResult = forecastResult,
        )
        return ActivitiesViewModel(
            searchCitiesUseCase = SearchCitiesUseCase(repository),
            getActivityRecommendationsUseCase = GetActivityRecommendationsUseCase(
                weatherRepository = repository,
                recommendationEngine = ActivityRecommendationEngine(),
            ),
        )
    }

    private class FakeWeatherRepository(
        private val searchResult: DomainResult<List<City>>,
        private val forecastResult: DomainResult<List<DailyForecast>>,
    ) : WeatherRepository {
        override suspend fun searchCities(query: String): DomainResult<List<City>> = searchResult

        override suspend fun getDailyForecast(
            latitude: Double,
            longitude: Double,
        ): DomainResult<List<DailyForecast>> = forecastResult
    }

    private companion object {
        val london = City(
            id = 2643743L,
            name = "London",
            country = "United Kingdom",
            admin1 = "England",
            latitude = 51.5,
            longitude = -0.12,
            elevation = null,
        )
    }
}
