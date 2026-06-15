package com.example.metroweatherapp.data.repository

import com.example.metroweatherapp.data.network.NetworkMonitor
import com.example.metroweatherapp.data.remote.api.ForecastApi
import com.example.metroweatherapp.data.remote.api.GeocodingApi
import com.example.metroweatherapp.data.remote.mapper.toDomain
import com.example.metroweatherapp.domain.model.AppError
import com.example.metroweatherapp.domain.model.City
import com.example.metroweatherapp.domain.model.DailyForecast
import com.example.metroweatherapp.domain.repository.DomainResult
import com.example.metroweatherapp.domain.repository.WeatherRepository
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class WeatherRepositoryImpl @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val forecastApi: ForecastApi,
    private val networkMonitor: NetworkMonitor,
) : WeatherRepository {

    override suspend fun searchCities(query: String): DomainResult<List<City>> {
        if (!networkMonitor.isOnline()) {
            return DomainResult.Error(AppError.Network(NO_INTERNET_MESSAGE))
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                geocodingApi.searchCities(name = query).results.orEmpty().map { it.toDomain() }
            }.fold(
                onSuccess = { DomainResult.Success(it) },
                onFailure = { DomainResult.Error(it.toAppError()) },
            )
        }
    }

    override suspend fun getDailyForecast(
        latitude: Double,
        longitude: Double,
    ): DomainResult<List<DailyForecast>> {
        if (!networkMonitor.isOnline()) {
            return DomainResult.Error(AppError.Network(NO_INTERNET_MESSAGE))
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                forecastApi.getForecast(latitude = latitude, longitude = longitude)
                    .daily
                    ?.toDomain()
                    .orEmpty()
            }.fold(
                onSuccess = { forecast ->
                    if (forecast.isEmpty()) {
                        DomainResult.Error(AppError.Unknown("No forecast data returned."))
                    } else {
                        DomainResult.Success(forecast)
                    }
                },
                onFailure = { DomainResult.Error(it.toAppError()) },
            )
        }
    }

    private fun Throwable.toAppError(): AppError {
        return when (this) {
            is UnknownHostException -> AppError.Network(
                "Cannot reach weather server. Your device may be offline or DNS is unavailable.",
            )
            is ConnectException -> AppError.Network(
                "Connection failed. Check that the emulator or device has internet access.",
            )
            is SocketTimeoutException -> AppError.Network("Request timed out. Try again.")
            is IOException -> AppError.Network()
            is HttpException -> {
                if (code() in 500..599 || code() == 429) {
                    AppError.Network("Weather service unavailable. Try again later.")
                } else {
                    AppError.Unknown(message())
                }
            }
            else -> AppError.Unknown(message)
        }
    }

    companion object {
        private const val NO_INTERNET_MESSAGE =
            "No internet connection on this device. Turn on Wi‑Fi or mobile data, or restart the emulator."
    }
}
