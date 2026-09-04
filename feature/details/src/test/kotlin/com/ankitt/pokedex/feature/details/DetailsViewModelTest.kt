package com.ankitt.pokedex.feature.details

import com.ankitt.pokedex.core.data.repository.PokemonRepository
import com.ankitt.pokedex.core.model.Pokemon
import com.ankitt.pokedex.core.test.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pokemonRepository: PokemonRepository = mock()

    private val bulbasaur = Pokemon(id = 1, name = "bulbasaur", imageUrl = "url-1")

    @Test
    fun `loadPokemon fetches from the repository and surfaces a successful result`() = runTest {
        whenever(pokemonRepository.getPokemon(eq("bulbasaur"), any())).thenReturn(flowOf(bulbasaur))
        val viewModel = DetailsViewModel(pokemonRepository)

        viewModel.loadPokemon("bulbasaur")

        assertThat(viewModel.uiState.value).isEqualTo(DetailsUiState.Success(bulbasaur))
    }

    @Test
    fun `loadPokemon is a no-op when the same name is already loaded`() = runTest {
        whenever(pokemonRepository.getPokemon(eq("bulbasaur"), any())).thenReturn(flowOf(bulbasaur))
        val viewModel = DetailsViewModel(pokemonRepository)

        viewModel.loadPokemon("bulbasaur")
        viewModel.loadPokemon("bulbasaur")

        verify(pokemonRepository, times(1)).getPokemon(eq("bulbasaur"), any())
    }

    @Test
    fun `a repository error surfaces as an error state`() = runTest {
        whenever(pokemonRepository.getPokemon(eq("bulbasaur"), any())).thenAnswer { invocation ->
            invocation.getArgument<(String) -> Unit>(1).invoke("network down")
            flowOf(null)
        }
        val viewModel = DetailsViewModel(pokemonRepository)

        viewModel.loadPokemon("bulbasaur")

        assertThat(viewModel.uiState.value).isEqualTo(DetailsUiState.Error("network down"))
    }

    @Test
    fun `retry bypasses the already-loaded guard and re-fetches the same pokemon`() = runTest {
        whenever(pokemonRepository.getPokemon(eq("bulbasaur"), any())).thenReturn(flowOf(bulbasaur))
        val viewModel = DetailsViewModel(pokemonRepository)
        viewModel.loadPokemon("bulbasaur")

        viewModel.retry()

        verify(pokemonRepository, times(2)).getPokemon(eq("bulbasaur"), any())
    }
}
