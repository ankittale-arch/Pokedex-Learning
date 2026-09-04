package com.ankitt.pokedex.core.database.di

import android.content.Context
import androidx.room.Room
import com.ankitt.pokedex.core.database.PokedexDatabase
import com.ankitt.pokedex.core.database.PokemonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "pokedex-database"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesPokedexDatabase(@ApplicationContext context: Context): PokedexDatabase =
        Room.databaseBuilder(context, PokedexDatabase::class.java, DATABASE_NAME)
            // No migrations yet at this stage - a schema bump just rebuilds the cache from
            // the network rather than crashing on devices with an older local copy.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providesPokemonDao(database: PokedexDatabase): PokemonDao = database.pokemonDao()
}
