package com.ankitt.pokedex.core.data.repository

import com.ankitt.pokedex.core.common.Dispatcher
import com.ankitt.pokedex.core.common.PokedexDispatchers
import com.ankitt.pokedex.core.database.PokemonDao
import com.ankitt.pokedex.core.database.model.PokemonEntity
import com.ankitt.pokedex.core.database.model.asExternalModel
import com.ankitt.pokedex.core.model.Pokemon
import com.ankitt.pokedex.core.model.isEmpty
import com.ankitt.pokedex.core.network.ApiResponse
import com.ankitt.pokedex.core.network.PokeApiService
import com.ankitt.pokedex.core.network.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val POKEMON_PAGE_SIZE = 20

/**
 * Offline-first: Room is the single source of truth for the UI - [getPokemonList] only ever
 * reads from it. Network access is explicit: [loadPokemonPage] fetches one page and merges it
 * into Room (preserving `types`/`stats`/`typeEffectiveness` on any row a detail fetch already
 * enriched), which is what both pagination and pull-to-refresh call into. [getPokemon] keeps
 * the original single-shot offline-first shape, since a detail lookup isn't paginated.
 */
class PokemonRepositoryImpl @Inject constructor(
    private val pokeApiService: PokeApiService,
    private val pokemonDao: PokemonDao,
    @Dispatcher(PokedexDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : PokemonRepository {

    override fun getPokemonList(): Flow<List<Pokemon>> =
        pokemonDao.getPokemonList()
            .map { it.map(PokemonEntity::asExternalModel) }
            .flowOn(ioDispatcher)

    override suspend fun loadPokemonPage(offset: Int, onError: (String) -> Unit) =
        withContext(ioDispatcher) {
            when (val response = safeApiCall { pokeApiService.getPokemonList(POKEMON_PAGE_SIZE, offset) }) {
                is ApiResponse.Success -> {
                    val merged = response.data.results.map { item ->
                        val fresh = item.asEntity()
                        val existing = pokemonDao.getPokemonById(fresh.id)
                        if (existing != null) {
                            fresh.copy(
                                types = existing.types,
                                stats = existing.stats,
                                typeEffectiveness = existing.typeEffectiveness,
                            )
                        } else {
                            fresh
                        }
                    }
                    pokemonDao.upsertAll(merged)
                }
                is ApiResponse.Error -> onError("Server error: ${response.code}")
                is ApiResponse.Exception -> onError(response.throwable.message ?: "Unknown error")
            }
        }

    override fun getPokemon(name: String, onError: (String) -> Unit): Flow<Pokemon?> = flow {
        val cached = pokemonDao.getPokemon(name).first()
        if (cached == null || cached.types.isEmpty() || cached.typeEffectiveness.isEmpty()) {
            when (val response = safeApiCall { pokeApiService.getPokemon(name) }) {
                is ApiResponse.Success -> {
                    val detail = response.data
                    val typeDetails = detail.types.mapNotNull { slot ->
                        when (val typeResponse = safeApiCall { pokeApiService.getTypeDetails(slot.type.name) }) {
                            is ApiResponse.Success -> typeResponse.data
                            else -> null
                        }
                    }
                    pokemonDao.upsert(detail.asEntity().copy(typeEffectiveness = combineTypeEffectiveness(typeDetails)))
                }
                is ApiResponse.Error -> onError("Server error: ${response.code}")
                is ApiResponse.Exception -> onError(response.throwable.message ?: "Unknown error")
            }
            emitAll(pokemonDao.getPokemon(name).map { it?.asExternalModel() })
        } else {
            emit(cached.asExternalModel())
        }
    }.flowOn(ioDispatcher)
}
