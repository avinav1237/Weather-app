package com.example.metroweatherapp.di

import com.example.metroweatherapp.data.network.NetworkMonitor
import com.example.metroweatherapp.data.network.NetworkMonitorImpl
import com.example.metroweatherapp.data.repository.WeatherRepositoryImpl
import com.example.metroweatherapp.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        networkMonitorImpl: NetworkMonitorImpl,
    ): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl,
    ): WeatherRepository
}
