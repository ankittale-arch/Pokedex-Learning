package com.ankitt.pokedex.core.data.repository

import com.ankitt.pokedex.core.database.model.PokemonEntity
import com.ankitt.pokedex.core.model.PokemonStat
import com.ankitt.pokedex.core.network.model.PokemonDetailDto
import com.ankitt.pokedex.core.network.model.PokemonListItemDto

/** [PokemonListItemDto.url] looks like ".../pokemon/25/" - the trailing segment is the id. */
private fun spriteUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

fun PokemonListItemDto.asEntity(): PokemonEntity {
    val id = url.trimEnd('/').substringAfterLast('/').toInt()
    return PokemonEntity(id = id, name = name, imageUrl = spriteUrl(id), types = emptyList())
}

fun PokemonDetailDto.asEntity(): PokemonEntity =
    PokemonEntity(
        id = id,
        name = name,
        imageUrl = sprites.frontDefault ?: spriteUrl(id),
        types = types.sortedBy { it.slot }.map { it.type.name },
        stats = stats.map { PokemonStat(name = it.stat.name, value = it.baseStat) },
    )
