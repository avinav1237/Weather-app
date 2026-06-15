package com.example.metroweatherapp.data.remote.mapper

import com.example.metroweatherapp.data.remote.dto.DailyForecastDto
import com.example.metroweatherapp.data.remote.dto.GeocodingResultDto
import com.example.metroweatherapp.domain.model.City
import com.example.metroweatherapp.domain.model.DailyForecast

fun GeocodingResultDto.toDomain(): City {
    return City(
        id = id,
        name = name,
        country = country.orEmpty(),
        admin1 = admin1,
        latitude = latitude,
        longitude = longitude,
        elevation = elevation,
    )
}

fun DailyForecastDto.toDomain(): List<DailyForecast> {
    val dates = time.orEmpty()
    if (dates.isEmpty()) return emptyList()

    return dates.mapIndexed { index, date ->
        DailyForecast(
            date = date,
            temperatureMaxC = temperatureMax?.getOrNull(index),
            temperatureMinC = temperatureMin?.getOrNull(index),
            precipitationMm = precipitationSum?.getOrNull(index),
            snowfallCm = snowfallSum?.getOrNull(index),
            windSpeedMaxKmh = windSpeedMax?.getOrNull(index),
            weatherCode = weatherCode?.getOrNull(index),
        )
    }
}
