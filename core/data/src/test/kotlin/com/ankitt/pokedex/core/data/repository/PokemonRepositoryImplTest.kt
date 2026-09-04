package com.ankitt.pokedex.core.data.repository

import com.ankitt.pokedex.core.database.PokemonDao
import com.ankitt.pokedex.core.database.model.PokemonEntity
import com.ankitt.pokedex.core.model.Pokemon
import com.ankitt.pokedex.core.model.PokemonStat
import com.ankitt.pokedex.core.model.TypeEffectiveness
import com.ankitt.pokedex.core.network.PokeApiService
import com.ankitt.pokedex.core.network.model.DamageRelationsDto
import com.ankitt.pokedex.core.network.model.NamedApiResourceDto
import com.ankitt.pokedex.core.network.model.PokemonDetailDto
import com.ankitt.pokedex.core.network.model.PokemonListItemDto
import com.ankitt.pokedex.core.network.model.PokemonListResponseDto
import com.ankitt.pokedex.core.network.model.PokemonTypeSlotDto
import com.ankitt.pokedex.core.network.model.SpritesDto
import com.ankitt.pokedex.core.network.model.TypeDetailsDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

private const val SPRITE_1 = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
private const val SPRITE_4 = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png"

class PokemonRepositoryImplTest {

    private val pokeApiService: PokeApiService = mock()
    private val pokemonDao: PokemonDao = mock()

    private lateinit var repository: PokemonRepositoryImpl

    @Before
    fun setUp() {
        repository = PokemonRepositoryImpl(pokeApiService, pokemonDao, UnconfinedTestDispatcher())
    }

    @Test
    fun `getPokemonList maps every cached entity into an external pokemon model`() = runTest {
        whenever(pokemonDao.getPokemonList()).thenReturn(
            flowOf(
                listOf(
                    PokemonEntity(id = 1, name = "bulbasaur", imageUrl = "url-1", types = listOf("grass")),
                    PokemonEntity(id = 4, name = "charmander", imageUrl = "url-4", types = listOf("fire")),
                ),
            ),
        )

        val result = repository.getPokemonList().first()

        assertThat(result).containsExactly(
            Pokemon(id = 1, name = "bulbasaur", imageUrl = "url-1", types = listOf("grass")),
            Pokemon(id = 4, name = "charmander", imageUrl = "url-4", types = listOf("fire")),
        ).inOrder()
    }

    @Test
    fun `loadPokemonPage merges a fresh page into room, preserving details an earlier fetch already cached`() =
        runTest {
            whenever(pokeApiService.getPokemonList(POKEMON_PAGE_SIZE, 0)).thenReturn(
                PokemonListResponseDto(
                    results = listOf(
                        PokemonListItemDto(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/"),
                        PokemonListItemDto(name = "charmander", url = "https://pokeapi.co/api/v2/pokemon/4/"),
                    ),
                ),
            )
            // bulbasaur was already enriched by a prior details fetch - that detail must survive the
            // page merge instead of being blanked back out to the bare list-item shape.
            val existingBulbasaur = PokemonEntity(
                id = 1,
                name = "bulbasaur",
                imageUrl = "old-url-1",
                types = listOf("grass", "poison"),
                stats = listOf(PokemonStat("hp", 45)),
                typeEffectiveness = TypeEffectiveness(weakTo = listOf("fire")),
            )
            whenever(pokemonDao.getPokemonById(1)).thenReturn(existingBulbasaur)
            whenever(pokemonDao.getPokemonById(4)).thenReturn(null)

            var errorMessage: String? = null
            repository.loadPokemonPage(offset = 0, onError = { errorMessage = it })

            assertThat(errorMessage).isNull()
            verify(pokemonDao).upsertAll(
                listOf(
                    existingBulbasaur.copy(imageUrl = SPRITE_1),
                    PokemonEntity(id = 4, name = "charmander", imageUrl = SPRITE_4, types = emptyList()),
                ),
            )
        }

    @Test
    fun `loadPokemonPage reports the HTTP status on a server error and never touches room`() = runTest {
        val httpException = HttpException(Response.error<Any>(404, "".toResponseBody(null)))
        whenever(pokeApiService.getPokemonList(any(), any())).thenAnswer { throw httpException }

        var errorMessage: String? = null
        repository.loadPokemonPage(offset = 0, onError = { errorMessage = it })

        assertThat(errorMessage).isEqualTo("Server error: 404")
        verifyNoInteractions(pokemonDao)
    }

    @Test
    fun `loadPokemonPage reports the exception message when the call throws`() = runTest {
        whenever(pokeApiService.getPokemonList(any(), any())).thenAnswer { throw IOException("no network") }

        var errorMessage: String? = null
        repository.loadPokemonPage(offset = 0, onError = { errorMessage = it })

        assertThat(errorMessage).isEqualTo("no network")
        verifyNoInteractions(pokemonDao)
    }

    @Test
    fun `getPokemon returns the cached row directly once it already has types and effectiveness`() = runTest {
        val cached = PokemonEntity(
            id = 1,
            name = "bulbasaur",
            imageUrl = "url-1",
            types = listOf("grass", "poison"),
            typeEffectiveness = TypeEffectiveness(weakTo = listOf("fire")),
        )
        whenever(pokemonDao.getPokemon("bulbasaur")).thenReturn(flowOf(cached))

        val result = repository.getPokemon("bulbasaur").first()

        assertThat(result).isEqualTo(Pokemon(id = 1, name = "bulbasaur", imageUrl = "url-1", types = cached.types, typeEffectiveness = cached.typeEffectiveness))
        verifyNoInteractions(pokeApiService)
    }

    @Test
    fun `getPokemon fetches details and type effectiveness over the network when the cache is incomplete`() =
        runTest {
            // The first call (the initial cache check) finds nothing; the second (after the network
            // fetch upserts) sees the freshly written row - modeling Room's own read-after-write.
            val refreshed = PokemonEntity(
                id = 1,
                name = "bulbasaur",
                imageUrl = "sprite-url",
                types = listOf("grass"),
                typeEffectiveness = TypeEffectiveness(weakTo = listOf("fire")),
            )
            whenever(pokemonDao.getPokemon("bulbasaur")).thenReturn(flowOf(null), flowOf(refreshed))
            whenever(pokeApiService.getPokemon("bulbasaur")).thenReturn(
                PokemonDetailDto(
                    id = 1,
                    name = "bulbasaur",
                    sprites = SpritesDto(frontDefault = "sprite-url"),
                    types = listOf(PokemonTypeSlotDto(slot = 1, type = NamedApiResourceDto("grass"))),
                ),
            )
            whenever(pokeApiService.getTypeDetails("grass")).thenReturn(
                TypeDetailsDto(
                    id = 12,
                    name = "grass",
                    damageRelations = DamageRelationsDto(doubleDamageFrom = listOf(NamedApiResourceDto("fire"))),
                ),
            )

            val result = repository.getPokemon("bulbasaur").toList()

            assertThat(result).containsExactly(
                Pokemon(
                    id = 1,
                    name = "bulbasaur",
                    imageUrl = "sprite-url",
                    types = listOf("grass"),
                    typeEffectiveness = TypeEffectiveness(weakTo = listOf("fire")),
                ),
            )
            verify(pokemonDao).upsert(eq(refreshed))
        }

    @Test
    fun `getPokemon surfaces a network error but still emits whatever room already had`() = runTest {
        whenever(pokemonDao.getPokemon("bulbasaur")).thenReturn(flowOf(null))
        whenever(pokeApiService.getPokemon("bulbasaur")).thenAnswer { throw IOException("no network") }

        var errorMessage: String? = null
        val result = repository.getPokemon("bulbasaur", onError = { errorMessage = it }).first()

        assertThat(errorMessage).isEqualTo("no network")
        assertThat(result).isNull()
        verify(pokemonDao, never()).upsert(any())
    }
}
