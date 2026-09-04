package com.ankitt.pokedex.feature.details

import com.ankitt.pokedex.core.model.Pokemon

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val pokemon: Pokemon) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}
