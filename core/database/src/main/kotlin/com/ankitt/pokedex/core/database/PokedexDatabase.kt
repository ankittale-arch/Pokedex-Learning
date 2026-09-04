package com.ankitt.pokedex.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ankitt.pokedex.core.database.model.PokemonEntity

// `version` only needs bumping when PokemonEntity's shape changes - see DatabaseModule's
// `fallbackToDestructiveMigration`, which just drops and rebuilds the cache from the network
// rather than requiring a real Migration for every bump, since this table is a disposable cache.
@Database(entities = [PokemonEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}
