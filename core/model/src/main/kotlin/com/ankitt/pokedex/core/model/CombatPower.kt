package com.ankitt.pokedex.core.model

import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

/** Perfect individual values - this app has no per-catch IV concept, so it assumes the best case. */
private const val PERFECT_IV = 15

/**
 * CP multiplier at trainer level 20. This app has no trainer-level concept either, so CP is
 * pinned to this one reference level rather than being level-selectable.
 * See https://gamepress.gg/pokemongo/cp-multiplier for the full level table.
 */
private const val CPM_LEVEL_20 = 0.5974

/**
 * Combat Power, from the real Pokemon GO formula:
 * `CP = floor((Attack + AtkIV) * sqrt(Defense + DefIV) * sqrt(Stamina + StaIV) * CPM^2 / 10)`
 *
 * PokeAPI only exposes each species' main-series base stats, not Pokemon GO's own (separately
 * rebalanced) Attack/Defense/Stamina table, so this is necessarily an approximation using the
 * closest main-series stats rather than the real in-game CP.
 */
fun calculateCp(
    attack: Int,
    defense: Int,
    stamina: Int,
    attackIv: Int = PERFECT_IV,
    defenseIv: Int = PERFECT_IV,
    staminaIv: Int = PERFECT_IV,
    cpMultiplier: Double = CPM_LEVEL_20,
): Int {
    val cp = (attack + attackIv) *
        sqrt((defense + defenseIv).toDouble()) *
        sqrt((stamina + staminaIv).toDouble()) *
        cpMultiplier.pow(2) /
        10.0
    return floor(cp).toInt()
}

/** Approximates GO's Attack/Defense/Stamina with this Pokemon's main-series attack/defense/hp. */
fun Pokemon.calculateCp(): Int = calculateCp(
    attack = stats.baseStatValue("attack"),
    defense = stats.baseStatValue("defense"),
    stamina = stats.baseStatValue("hp"),
)

private fun List<PokemonStat>.baseStatValue(name: String): Int = find { it.name == name }?.value ?: 0
