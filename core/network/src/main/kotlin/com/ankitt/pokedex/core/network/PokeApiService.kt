package com.ankitt.pokedex.core.network

import com.ankitt.pokedex.core.network.model.PokemonDetailDto
import com.ankitt.pokedex.core.network.model.PokemonListResponseDto
import com.ankitt.pokedex.core.network.model.TypeDetailsDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): PokemonListResponseDto

    @GET("pokemon/{name}")
    suspend fun getPokemon(@Path("name") name: String): PokemonDetailDto

    @GET("type/{id_or_name}")
    suspend fun getTypeDetails(@Path("id_or_name") typeNameOrId: String): TypeDetailsDto
}
