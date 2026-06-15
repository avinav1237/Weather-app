package com.example.metroweatherapp.domain.model

data class DailyForecast(
    val date: String,
    val temperatureMaxC: Double?,
    val temperatureMinC: Double?,
    val precipitationMm: Double?,
    val snowfallCm: Double?,
    val windSpeedMaxKmh: Double?,
    val weatherCode: Int?,
)
