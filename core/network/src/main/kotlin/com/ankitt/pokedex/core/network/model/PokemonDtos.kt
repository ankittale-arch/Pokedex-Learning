package com.ankitt.pokedex.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponseDto(
    val results: List<PokemonListItemDto>,
)

@Serializable
data class PokemonListItemDto(
    val name: String,
    val url: String,
)

@Serializable
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val sprites: SpritesDto,
    val types: List<PokemonTypeSlotDto>,
    val stats: List<PokemonStatDto> = emptyList(),
)

@Serializable
data class PokemonStatDto(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedApiResourceDto,
)

@Serializable
data class SpritesDto(
    @SerialName("front_default") val frontDefault: String?,
)

@Serializable
data class PokemonTypeSlotDto(
    val slot: Int,
    val type: NamedApiResourceDto,
)

@Serializable
data class NamedApiResourceDto(
    val name: String,
)
