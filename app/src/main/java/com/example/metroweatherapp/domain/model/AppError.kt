package com.example.metroweatherapp.domain.model

sealed interface AppError {
    data class Network(
        val message: String = "Network error. Check your connection and try again.",
    ) : AppError

    data class Unknown(val message: String? = null) : AppError
}
