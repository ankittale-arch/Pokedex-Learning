package com.ankitt.pokedex.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.pokedex.core.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var loadedName: String? = null

    fun loadPokemon(name: String) {
        if (loadedName == name) return
        loadedName = name
        fetch(name)
    }

    /** Re-runs the same fetch after a failure - bypasses the [loadedName] "already loaded" guard. */
    fun retry() {
        loadedName?.let { name ->
            _uiState.update { DetailsUiState.Loading }
            fetch(name)
        }
    }

    private fun fetch(name: String) {
        viewModelScope.launch {
            pokemonRepository
                .getPokemon(name, onError = { message -> _uiState.update { DetailsUiState.Error(message) } })
                .collect { pokemon ->
                    if (pokemon != null) _uiState.update { DetailsUiState.Success(pokemon) }
                }
        }
    }
}
