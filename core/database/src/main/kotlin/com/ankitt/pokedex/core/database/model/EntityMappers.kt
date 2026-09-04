package com.ankitt.pokedex.core.database.model

import com.ankitt.pokedex.core.model.Pokemon

fun PokemonEntity.asExternalModel(): Pokemon =
    Pokemon(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types,
        stats = stats,
        typeEffectiveness = typeEffectiveness,
    )
