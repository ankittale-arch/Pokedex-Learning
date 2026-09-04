package com.ankitt.pokedex.core.network.di

import com.ankitt.pokedex.core.network.connectivity.ConnectivityManagerNetworkMonitor
import com.ankitt.pokedex.core.network.connectivity.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {

    @Binds
    abstract fun bindsNetworkMonitor(impl: ConnectivityManagerNetworkMonitor): NetworkMonitor
}
