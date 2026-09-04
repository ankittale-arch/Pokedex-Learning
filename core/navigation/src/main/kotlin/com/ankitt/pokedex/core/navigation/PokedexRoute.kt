package com.ankitt.pokedex.core.navigation

import kotlinx.serialization.Serializable

/**
 * Shared, type-safe navigation contracts. Feature modules depend on this to declare
 * their destinations and to navigate to each other's screens without depending on
 * each other directly - only `app` wires every feature's graph together.
 */
sealed interface PokedexRoute {

    @Serializable
    data object Home : PokedexRoute

    @Serializable
    data class Details(val pokemonName: String) : PokedexRoute
}
