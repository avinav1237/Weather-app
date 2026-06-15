package com.example.metroweatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val daily: DailyForecastDto? = null,
)

@Serializable
data class DailyForecastDto(
    val time: List<String>? = null,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double>? = null,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double>? = null,
    @SerialName("precipitation_sum") val precipitationSum: List<Double>? = null,
    @SerialName("snowfall_sum") val snowfallSum: List<Double>? = null,
    @SerialName("wind_speed_10m_max") val windSpeedMax: List<Double>? = null,
    @SerialName("weather_code") val weatherCode: List<Int>? = null,
)
