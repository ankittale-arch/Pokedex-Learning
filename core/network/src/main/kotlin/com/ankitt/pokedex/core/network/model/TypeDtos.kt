package com.ankitt.pokedex.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TypeDetailsDto(
    val id: Int,
    val name: String,
    @SerialName("damage_relations") val damageRelations: DamageRelationsDto,
)

/**
 * Only the "*_from" relations are modeled - they're the ones that answer "what damage does a
 * Pokemon of this type take", which is the only direction the details screen cares about.
 */
@Serializable
data class DamageRelationsDto(
    @SerialName("double_damage_from") val doubleDamageFrom: List<NamedApiResourceDto> = emptyList(),
    @SerialName("half_damage_from") val halfDamageFrom: List<NamedApiResourceDto> = emptyList(),
    @SerialName("no_damage_from") val noDamageFrom: List<NamedApiResourceDto> = emptyList(),
)
