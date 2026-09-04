package com.ankitt.pokedex.core.model

/** What damage a Pokemon takes, combined across all of its types. */
data class TypeEffectiveness(
    val weakTo: List<String> = emptyList(),
    val resistantTo: List<String> = emptyList(),
    val immuneTo: List<String> = emptyList(),
)

fun TypeEffectiveness.isEmpty(): Boolean = weakTo.isEmpty() && resistantTo.isEmpty() && immuneTo.isEmpty()
