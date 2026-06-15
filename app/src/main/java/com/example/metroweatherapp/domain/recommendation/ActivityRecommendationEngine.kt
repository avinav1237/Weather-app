package com.example.metroweatherapp.domain.recommendation

import com.example.metroweatherapp.domain.model.ActivityType
import com.example.metroweatherapp.domain.model.DailyForecast
import com.example.metroweatherapp.domain.model.RankedActivity
import javax.inject.Inject

class ActivityRecommendationEngine @Inject constructor() {

    fun rankActivities(forecast: List<DailyForecast>): List<RankedActivity> {
        if (forecast.isEmpty()) return emptyList()

        val skiingDaysWithSnow = forecast.count { scoreSkiing(it) >= 60 }
        val windyDays = forecast.count { scoreSurfing(it) >= 60 }
        val pleasantOutdoorDays = forecast.count { scoreOutdoorSightseeing(it) >= 60 }
        val poorOutdoorDays = forecast.count { scoreOutdoorSightseeing(it) <= 40 }

        return ActivityType.entries.map { activity ->
            val averageScore = forecast.map { scoreForActivity(activity, it) }.average()
            val score = averageScore.toInt().coerceIn(0, 100)
            RankedActivity(
                activity = activity,
                score = score,
                summary = buildSummary(
                    activity = activity,
                    score = score,
                    skiingDaysWithSnow = skiingDaysWithSnow,
                    windyDays = windyDays,
                    pleasantOutdoorDays = pleasantOutdoorDays,
                    poorOutdoorDays = poorOutdoorDays,
                ),
            )
        }.sortedByDescending { it.score }
    }

    private fun scoreForActivity(activity: ActivityType, day: DailyForecast): Int {
        return when (activity) {
            ActivityType.SKIING -> scoreSkiing(day)
            ActivityType.SURFING -> scoreSurfing(day)
            ActivityType.OUTDOOR_SIGHTSEEING -> scoreOutdoorSightseeing(day)
            ActivityType.INDOOR_SIGHTSEEING -> scoreIndoorSightseeing(day)
        }
    }

    private fun scoreSkiing(day: DailyForecast): Int {
        val maxTemp = day.temperatureMaxC ?: return 0
        val snowfall = day.snowfallCm ?: 0.0
        val precipitation = day.precipitationMm ?: 0.0

        val coldScore = when {
            maxTemp <= -5 -> 100
            maxTemp <= 0 -> linearScore(maxTemp, from = 0.0, to = -5.0, fromScore = 80.0, toScore = 100.0)
            maxTemp <= 5 -> linearScore(maxTemp, from = 5.0, to = 0.0, fromScore = 20.0, toScore = 80.0)
            else -> 0
        }

        val snowScore = when {
            snowfall >= 5 -> 100
            snowfall >= 1 -> linearScore(snowfall, from = 1.0, to = 5.0, fromScore = 60.0, toScore = 100.0)
            snowfall > 0 -> 40
            else -> if (maxTemp <= 2 && precipitation > 0) 30 else 0
        }

        return ((coldScore * 0.55) + (snowScore * 0.45)).toInt().coerceIn(0, 100)
    }

    private fun scoreSurfing(day: DailyForecast): Int {
        val wind = day.windSpeedMaxKmh ?: return 0
        val maxTemp = day.temperatureMaxC ?: return 0
        val precipitation = day.precipitationMm ?: 0.0

        val windScore = when {
            wind in 15.0..35.0 -> 100
            wind in 10.0..15.0 -> linearScore(wind, from = 10.0, to = 15.0, fromScore = 50.0, toScore = 100.0)
            wind in 35.0..45.0 -> linearScore(wind, from = 35.0, to = 45.0, fromScore = 100.0, toScore = 40.0)
            wind < 10 -> linearScore(wind, from = 0.0, to = 10.0, fromScore = 10.0, toScore = 50.0)
            else -> 20
        }

        val tempScore = when {
            maxTemp in 12.0..28.0 -> 100
            maxTemp in 8.0..12.0 -> linearScore(maxTemp, from = 8.0, to = 12.0, fromScore = 40.0, toScore = 100.0)
            maxTemp in 28.0..32.0 -> linearScore(maxTemp, from = 28.0, to = 32.0, fromScore = 100.0, toScore = 50.0)
            else -> 20
        }

        val dryScore = when {
            precipitation <= 1 -> 100
            precipitation <= 5 -> linearScore(precipitation, from = 1.0, to = 5.0, fromScore = 100.0, toScore = 50.0)
            else -> 20
        }

        return ((windScore * 0.45) + (tempScore * 0.30) + (dryScore * 0.25)).toInt().coerceIn(0, 100)
    }

    private fun scoreOutdoorSightseeing(day: DailyForecast): Int {
        val maxTemp = day.temperatureMaxC ?: return 0
        val minTemp = day.temperatureMinC ?: maxTemp
        val avgTemp = (maxTemp + minTemp) / 2.0
        val precipitation = day.precipitationMm ?: 0.0
        val wind = day.windSpeedMaxKmh ?: 0.0
        val weatherCode = day.weatherCode

        val tempScore = when {
            avgTemp in 15.0..26.0 -> 100
            avgTemp in 10.0..15.0 -> linearScore(avgTemp, from = 10.0, to = 15.0, fromScore = 60.0, toScore = 100.0)
            avgTemp in 26.0..30.0 -> linearScore(avgTemp, from = 26.0, to = 30.0, fromScore = 100.0, toScore = 60.0)
            avgTemp in 5.0..10.0 -> linearScore(avgTemp, from = 5.0, to = 10.0, fromScore = 30.0, toScore = 60.0)
            else -> 15
        }

        val precipitationScore = when {
            precipitation <= 0.5 -> 100
            precipitation <= 3 -> linearScore(precipitation, from = 0.5, to = 3.0, fromScore = 100.0, toScore = 50.0)
            precipitation <= 10 -> linearScore(precipitation, from = 3.0, to = 10.0, fromScore = 50.0, toScore = 10.0)
            else -> 0
        }

        val windScore = when {
            wind <= 20 -> 100
            wind <= 35 -> linearScore(wind, from = 20.0, to = 35.0, fromScore = 100.0, toScore = 40.0)
            else -> 20
        }

        val weatherScore = weatherCode?.let(::scoreWeatherCode) ?: 70

        return ((tempScore * 0.35) + (precipitationScore * 0.30) + (windScore * 0.15) + (weatherScore * 0.20))
            .toInt()
            .coerceIn(0, 100)
    }

    private fun scoreIndoorSightseeing(day: DailyForecast): Int {
        val outdoorScore = scoreOutdoorSightseeing(day)
        return (100 - outdoorScore).coerceIn(10, 100)
    }

    private fun scoreWeatherCode(code: Int): Int {
        return when (code) {
            0 -> 100
            in 1..3 -> 90
            in 45..48 -> 70
            in 51..57 -> 40
            in 61..67 -> 20
            in 71..77 -> 30
            in 80..82 -> 25
            in 95..99 -> 10
            else -> 60
        }
    }

    private fun linearScore(
        value: Double,
        from: Double,
        to: Double,
        fromScore: Double,
        toScore: Double,
    ): Int {
        if (from == to) return fromScore.toInt()
        val ratio = ((value - from) / (to - from)).coerceIn(0.0, 1.0)
        return (fromScore + (toScore - fromScore) * ratio).toInt()
    }

    private fun buildSummary(
        activity: ActivityType,
        score: Int,
        skiingDaysWithSnow: Int,
        windyDays: Int,
        pleasantOutdoorDays: Int,
        poorOutdoorDays: Int,
    ): String {
        return when (activity) {
            ActivityType.SKIING -> when {
                score >= 70 -> "Cold conditions with snowfall on $skiingDaysWithSnow of 7 days."
                score >= 40 -> "Some cold days, but limited snowfall for skiing."
                else -> "Warm or dry forecast; skiing conditions look poor."
            }
            ActivityType.SURFING -> when {
                score >= 70 -> "Moderate winds on $windyDays of 7 days suit surfing."
                score >= 40 -> "Mixed wind; some days may work for surfing."
                else -> "Calm or extreme winds make surfing unlikely."
            }
            ActivityType.OUTDOOR_SIGHTSEEING -> when {
                score >= 70 -> "Pleasant outdoor weather on $pleasantOutdoorDays of 7 days."
                score >= 40 -> "Mixed conditions; pick dry, mild days for sightseeing."
                else -> "Rain or extreme temperatures limit outdoor sightseeing."
            }
            ActivityType.INDOOR_SIGHTSEEING -> when {
                score >= 70 -> "Poor outdoor weather on $poorOutdoorDays of 7 days favors indoor plans."
                score >= 40 -> "Some rainy or extreme days; indoor options are a safe bet."
                else -> "Mostly good outdoor weather; indoor sightseeing is less compelling."
            }
        }
    }
}
