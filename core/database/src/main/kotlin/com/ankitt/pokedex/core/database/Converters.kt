package com.ankitt.pokedex.core.database

import androidx.room.TypeConverter
import com.ankitt.pokedex.core.model.PokemonStat
import com.ankitt.pokedex.core.model.TypeEffectiveness

/**
 * Room can only persist primitive columns, so every list/object field on [PokemonEntity][
 * com.ankitt.pokedex.core.database.model.PokemonEntity] needs a converter to/from a single
 * `TEXT` column. Hand-rolled delimited strings (`,`/`;`) are used instead of JSON so this
 * module doesn't need to pull in a serialization dependency just for a handful of flat string
 * lists - none of the values themselves can ever contain a comma or semicolon (Pokemon/type/
 * stat names from the API), so no escaping is needed.
 */
internal class Converters {

    @TypeConverter
    fun fromTypesList(types: List<String>): String = types.joinToString(",")

    @TypeConverter
    fun toTypesList(raw: String): List<String> =
        if (raw.isEmpty()) emptyList() else raw.split(",")

    @TypeConverter
    fun fromStatsList(stats: List<PokemonStat>): String =
        stats.joinToString(";") { "${it.name}:${it.value}" }

    @TypeConverter
    fun toStatsList(raw: String): List<PokemonStat> =
        if (raw.isEmpty()) {
            emptyList()
        } else {
            raw.split(";").map { entry ->
                val (name, value) = entry.split(":")
                PokemonStat(name = name, value = value.toInt())
            }
        }

    @TypeConverter
    fun fromTypeEffectiveness(effectiveness: TypeEffectiveness): String =
        listOf(effectiveness.weakTo, effectiveness.resistantTo, effectiveness.immuneTo)
            .joinToString(";") { it.joinToString(",") }

    @TypeConverter
    fun toTypeEffectiveness(raw: String): TypeEffectiveness {
        val buckets = raw.split(";")
        fun bucket(index: Int): List<String> =
            buckets.getOrNull(index)?.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()
        return TypeEffectiveness(weakTo = bucket(0), resistantTo = bucket(1), immuneTo = bucket(2))
    }
}
