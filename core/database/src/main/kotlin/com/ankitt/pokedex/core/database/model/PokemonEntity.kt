package com.ankitt.pokedex.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ankitt.pokedex.core.model.PokemonStat
import com.ankitt.pokedex.core.model.TypeEffectiveness

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val stats: List<PokemonStat> = emptyList(),
    val typeEffectiveness: TypeEffectiveness = TypeEffectiveness(),
)
