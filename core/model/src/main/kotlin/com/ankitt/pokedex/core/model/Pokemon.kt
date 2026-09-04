package com.ankitt.pokedex.core.model

/**
 * Plain domain model. No Android, Room, or network annotations here -
 * those belong to the entity/DTO types that get mapped into this.
 */
data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String> = emptyList(),
    val stats: List<PokemonStat> = emptyList(),
    val typeEffectiveness: TypeEffectiveness = TypeEffectiveness(),
)
