package com.ankitt.pokedex.feature.home

import com.ankitt.pokedex.core.data.repository.POKEMON_PAGE_SIZE
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

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pokemonRepository: PokemonRepository = mock()

    private val samplePokemonList = listOf(
        Pokemon(id = 1, name = "bulbasaur", imageUrl = "url-1"),
        Pokemon(id = 4, name = "charmander", imageUrl = "url-4"),
    )

    private fun createViewModel(): HomeViewModel {
        whenever(pokemonRepository.getPokemonList()).thenReturn(flowOf(samplePokemonList))
        return HomeViewModel(pokemonRepository)
    }

    @Test
    fun `on init, loads the first page and populates the list from the repository`() = runTest {
        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.pokemonList).isEqualTo(samplePokemonList)
        verify(pokemonRepository).loadPokemonPage(eq(0), any())
    }

    @Test
    fun `loadNextPage requests the next offset`() = runTest {
        val viewModel = createViewModel()

        viewModel.loadNextPage()

        verify(pokemonRepository).loadPokemonPage(eq(POKEMON_PAGE_SIZE), any())
    }

    @Test
    fun `refresh reloads offset 0 without disturbing pagination progress`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadNextPage() // advances the next offset to 2 * POKEMON_PAGE_SIZE

        viewModel.refresh()
        viewModel.loadNextPage()

        verify(pokemonRepository, times(2)).loadPokemonPage(eq(0), any())
        verify(pokemonRepository).loadPokemonPage(eq(2 * POKEMON_PAGE_SIZE), any())
    }

    @Test
    fun `a repository error surfaces as the error message in ui state`() = runTest {
        whenever(pokemonRepository.getPokemonList()).thenReturn(flowOf(emptyList()))
        whenever(pokemonRepository.loadPokemonPage(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<(String) -> Unit>(1).invoke("boom")
        }

        val viewModel = HomeViewModel(pokemonRepository)

        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("boom")
        assertThat(viewModel.uiState.value.isLoadingMore).isFalse()
    }
}
