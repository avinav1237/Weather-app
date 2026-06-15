package com.example.metroweatherapp.domain.model

data class RankedActivity(
    val activity: ActivityType,
    val score: Int,
    val summary: String,
)
