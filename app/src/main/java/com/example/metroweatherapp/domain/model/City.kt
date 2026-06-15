package com.example.metroweatherapp.domain.model

data class City(
    val id: Long,
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
) {
    val displayName: String
        get() = buildString {
            append(name)
            admin1?.takeIf { it.isNotBlank() }?.let { append(", $it") }
            if (country.isNotBlank()) append(", $country")
        }
}
