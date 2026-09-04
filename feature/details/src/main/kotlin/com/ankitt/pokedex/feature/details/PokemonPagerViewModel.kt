package com.ankitt.pokedex.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.pokedex.core.data.repository.PokemonRepository
import com.ankitt.pokedex.core.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Drives the swipeable pager on the details screen: just the ordered list of names the user
 * can swipe through (the same order Home shows). Each page still loads its own full detail -
 * see [DetailsViewModel] - this only knows the sequence, not any one Pokemon's data.
 */
@HiltViewModel
class PokemonPagerViewModel @Inject constructor(
    pokemonRepository: PokemonRepository,
) : ViewModel() {

    val pokemonNames: StateFlow<List<String>> = pokemonRepository.getPokemonList()
        .map { list -> list.map(Pokemon::name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())
}
