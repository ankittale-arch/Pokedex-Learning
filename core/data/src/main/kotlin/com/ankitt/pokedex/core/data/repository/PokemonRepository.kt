package com.ankitt.pokedex.core.data.repository

import com.ankitt.pokedex.core.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {

    /** Always reads from Room - the UI observes this continuously as pages get loaded into it. */
    fun getPokemonList(): Flow<List<Pokemon>>

    /**
     * Fetches one page (20 items) starting at [offset] and merges it into Room. Used both for
     * the initial load / "load next page on scroll" (increasing [offset]) and for pull-to-refresh
     * (`offset = 0`) - either way [getPokemonList] picks up the change since Room is the only
     * thing the UI actually observes.
     */
    suspend fun loadPokemonPage(offset: Int, onError: (String) -> Unit = {})

    /** [name] is looked up in Room; the network is only called when the cached row lacks types. */
    fun getPokemon(name: String, onError: (String) -> Unit = {}): Flow<Pokemon?>
}
