package com.ankitt.pokedex.core.data.repository

import com.ankitt.pokedex.core.model.TypeEffectiveness
import com.ankitt.pokedex.core.network.model.TypeDetailsDto

/**
 * Combines the defensive damage relations of every type a Pokemon has into one effectiveness
 * multiplier per attacking type - a dual-type Pokemon that's weak to a type via both of its own
 * types takes it twice, exactly like in the games.
 */
fun combineTypeEffectiveness(typeDetails: List<TypeDetailsDto>): TypeEffectiveness {
    val multipliers = mutableMapOf<String, Double>()
    typeDetails.forEach { details ->
        details.damageRelations.doubleDamageFrom.forEach { type ->
            multipliers[type.name] = (multipliers[type.name] ?: 1.0) * 2.0
        }
        details.damageRelations.halfDamageFrom.forEach { type ->
            multipliers[type.name] = (multipliers[type.name] ?: 1.0) * 0.5
        }
        details.damageRelations.noDamageFrom.forEach { type ->
            multipliers[type.name] = (multipliers[type.name] ?: 1.0) * 0.0
        }
    }
    return TypeEffectiveness(
        weakTo = multipliers.filterValues { it > 1.0 }.keys.sorted(),
        resistantTo = multipliers.filterValues { it in 0.0001..0.9999 }.keys.sorted(),
        immuneTo = multipliers.filterValues { it == 0.0 }.keys.sorted(),
    )
}
