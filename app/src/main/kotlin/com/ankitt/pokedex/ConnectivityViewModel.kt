package com.ankitt.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.pokedex.core.network.connectivity.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    // `initialValue = true` so the offline banner never flashes on first composition before the
    // real connectivity callback arrives. `WhileSubscribed(5_000)` keeps the OS connectivity
    // callback registered for 5s after the last collector goes away, so a config change (e.g.
    // rotation) doesn't tear down and immediately re-register the listener.
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = true)
}
