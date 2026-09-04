package com.ankitt.pokedex.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ankitt.pokedex.core.database.model.PokemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    fun getPokemonList(): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon WHERE name = :name")
    fun getPokemon(name: String): Flow<PokemonEntity?>

    /** One-shot lookup used to preserve `types` when a list page re-fetch would otherwise blank it. */
    @Query("SELECT * FROM pokemon WHERE id = :id")
    suspend fun getPokemonById(id: Int): PokemonEntity?

    @Upsert
    suspend fun upsertAll(pokemon: List<PokemonEntity>)

    @Upsert
    suspend fun upsert(pokemon: PokemonEntity)
}
