package com.ankitt.pokedex.core.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityManagerNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) : NetworkMonitor {

    // `callbackFlow` bridges ConnectivityManager's callback-based API into a cold Flow: the
    // block below runs once per collector, registers a NetworkCallback, and `awaitClose`
    // unregisters it when the collector (ConnectivityViewModel's viewModelScope) goes away -
    // otherwise the callback would leak past the ViewModel's lifetime.
    override val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            trySend(false)
            channel.close()
            return@callbackFlow
        }

        fun currentlyConnected(): Boolean =
            connectivityManager.activeNetwork
                ?.let(connectivityManager::getNetworkCapabilities)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentlyConnected())
            }

            override fun onLost(network: Network) {
                trySend(currentlyConnected())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(currentlyConnected())
            }
        }

        trySend(currentlyConnected())
        // An unconstrained NetworkRequest matches every transport (wifi, cellular, ethernet,
        // VPN, ...) so any of them coming up or down re-evaluates `currentlyConnected()`,
        // rather than tying this app to one specific transport type.
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        // The callbacks above fire once per network event, which can repeat the same
        // true/false verdict (e.g. wifi and cellular both flip while overall connectivity
        // doesn't change) - collapse those into actual state transitions only.
    }.distinctUntilChanged()
}
