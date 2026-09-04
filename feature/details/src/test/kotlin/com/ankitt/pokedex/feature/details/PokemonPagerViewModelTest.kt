package com.ankitt.pokedex.feature.details

import app.cash.turbine.test
import com.ankitt.pokedex.core.data.repository.PokemonRepository
import com.ankitt.pokedex.core.model.Pokemon
import com.ankitt.pokedex.core.test.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PokemonPagerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pokemonRepository: PokemonRepository = mock()

    @Test
    fun `exposes just the ordered pokemon names from the repository's list`() = runTest {
        val list = listOf(
            Pokemon(id = 1, name = "bulbasaur", imageUrl = "url-1"),
            Pokemon(id = 4, name = "charmander", imageUrl = "url-4"),
        )
        whenever(pokemonRepository.getPokemonList()).thenReturn(flowOf(list))
        val viewModel = PokemonPagerViewModel(pokemonRepository)

        viewModel.pokemonNames.test {
            // stateIn's seeded initial value (emptyList()) may or may not be observed before the
            // upstream flow's mapped result, depending on how eagerly WhileSubscribed sharing
            // starts - so skip over it if it shows up rather than asserting a fixed emission count.
            val first = awaitItem()
            val names = if (first.isEmpty()) awaitItem() else first
            assertThat(names).isEqualTo(listOf("bulbasaur", "charmander"))
        }
    }
}
