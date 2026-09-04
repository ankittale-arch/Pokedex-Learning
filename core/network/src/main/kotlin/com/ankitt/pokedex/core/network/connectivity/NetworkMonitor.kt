package com.ankitt.pokedex.core.network.connectivity

import kotlinx.coroutines.flow.Flow

/** Live device connectivity, so the UI can react the instant the network drops or comes back. */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
