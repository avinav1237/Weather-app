package com.example.metroweatherapp.domain.recommendation

import com.example.metroweatherapp.domain.model.ActivityType
import com.example.metroweatherapp.domain.model.DailyForecast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityRecommendationEngineTest {

    private val engine = ActivityRecommendationEngine()

    @Test
    fun `empty forecast returns empty list`() {
        val result = engine.rankActivities(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `cold snowy week ranks skiing first`() {
        val forecast = List(7) { index ->
            forecastDay(
                date = "2026-01-0${index + 1}",
                maxTemp = -8.0,
                minTemp = -12.0,
                snowfall = 6.0,
            )
        }

        val result = engine.rankActivities(forecast)

        assertEquals(ActivityType.SKIING, result.first().activity)
        assertTrue(result.first().score >= 90)
        assertTrue(result.last().score < result.first().score)
    }

    @Test
    fun `mild windy week ranks surfing above skiing`() {
        val forecast = List(7) { index ->
            forecastDay(
                date = "2026-06-1${index + 5}",
                maxTemp = 20.0,
                minTemp = 14.0,
                wind = 25.0,
            )
        }

        val result = engine.rankActivities(forecast)
        val skiing = result.first { it.activity == ActivityType.SKIING }
        val surfing = result.first { it.activity == ActivityType.SURFING }

        assertEquals(ActivityType.SURFING, result.first().activity)
        assertTrue(surfing.score > skiing.score)
    }

    @Test
    fun `results are sorted by score descending`() {
        val forecast = listOf(
            forecastDay(date = "2026-06-15", maxTemp = 20.0, minTemp = 14.0, wind = 25.0),
            forecastDay(date = "2026-06-16", maxTemp = 18.0, minTemp = 12.0, wind = 22.0),
            forecastDay(date = "2026-06-17", maxTemp = 16.0, minTemp = 10.0, wind = 18.0, precip = 2.0),
        )

        val scores = engine.rankActivities(forecast).map { it.score }

        assertEquals(scores, scores.sortedDescending())
    }

    private fun forecastDay(
        date: String,
        maxTemp: Double,
        minTemp: Double = maxTemp - 4.0,
        precip: Double = 0.0,
        snowfall: Double = 0.0,
        wind: Double = 10.0,
        weatherCode: Int = 0,
    ): DailyForecast {
        return DailyForecast(
            date = date,
            temperatureMaxC = maxTemp,
            temperatureMinC = minTemp,
            precipitationMm = precip,
            snowfallCm = snowfall,
            windSpeedMaxKmh = wind,
            weatherCode = weatherCode,
        )
    }
}
