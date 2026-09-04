package com.ankitt.pokedex.feature.home

import com.ankitt.pokedex.core.model.Pokemon

data class HomeUiState(
    val pokemonList: List<Pokemon> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
)
