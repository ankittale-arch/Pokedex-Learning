package com.ankitt.pokedex.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.pokedex.core.data.repository.POKEMON_PAGE_SIZE
import com.ankitt.pokedex.core.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** How many items have been requested from the network so far. */
    private var nextOffset = 0

    init {
        viewModelScope.launch {
            pokemonRepository.getPokemonList().collect { list ->
                _uiState.update { it.copy(pokemonList = list) }
            }
        }
        loadNextPage()
    }

    /** Called when the list is scrolled near its end. */
    fun loadNextPage() {
        if (_uiState.value.isLoadingMore) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            val offset = nextOffset
            pokemonRepository.loadPokemonPage(
                offset = offset,
                onError = { message -> _uiState.update { it.copy(errorMessage = message) } },
            )
            nextOffset = offset + POKEMON_PAGE_SIZE
            _uiState.update { it.copy(isLoadingMore = false) }
        }
    }

    /** Called from pull-to-refresh: re-fetches the first page without resetting pagination. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            pokemonRepository.loadPokemonPage(
                offset = 0,
                onError = { message -> _uiState.update { it.copy(errorMessage = message) } },
            )
            if (nextOffset == 0) nextOffset = POKEMON_PAGE_SIZE
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
