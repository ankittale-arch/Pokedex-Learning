package com.ankitt.pokedex.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ankitt.pokedex.core.navigation.PokedexRoute
import com.ankitt.pokedex.feature.details.DetailsPaneRoute
import com.ankitt.pokedex.feature.details.DetailsRoute
import com.ankitt.pokedex.feature.home.HomeRoute

/** Below this width, Home pushes Details on top of it (see [PokedexRoute]) as its own
 * destination - a phone screen isn't wide enough to show both at once. At and above it (small
 * and large tablets, and phones in landscape), [PokedexListDetailScreen] shows them side by
 * side permanently instead, matching Material's guidance for list-detail layouts at the medium
 * window size class and up. */
private const val LIST_DETAIL_MIN_WIDTH_DP = 600

/** `app` is the only module that knows about every feature - it wires their graphs together. */
@Composable
fun PokedexNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    if (LocalConfiguration.current.screenWidthDp >= LIST_DETAIL_MIN_WIDTH_DP) {
        PokedexListDetailScreen(modifier = modifier)
    } else {
        NavHost(navController = navController, startDestination = PokedexRoute.Home, modifier = modifier) {
            composable<PokedexRoute.Home> {
                HomeRoute(onPokemonClick = { name -> navController.navigate(PokedexRoute.Details(name)) })
            }
            composable<PokedexRoute.Details> { backStackEntry ->
                val details = backStackEntry.toRoute<PokedexRoute.Details>()
                DetailsRoute(
                    pokemonName = details.pokemonName,
                    onBackClick = { navController.popBackStack() },
                )
            }
        }
    }
}

/** The list pane's own fixed width - wide enough for [HomeRoute]'s grid to lay out at least two
 * columns, while leaving the rest of the screen (however wide) to the detail pane next to it. */
private val ListPaneWidth = 340.dp

/** List and detail side by side, both always on screen - selecting a Pokemon in the list just
 * swaps what the detail pane shows next to it, it never navigates away. Nothing selected yet
 * shows a plain placeholder in the detail pane instead of leaving it empty. */
@Composable
private fun PokedexListDetailScreen(modifier: Modifier = Modifier) {
    var selectedPokemon by rememberSaveable { mutableStateOf<String?>(null) }

    Row(modifier = modifier.fillMaxSize()) {
        HomeRoute(
            onPokemonClick = { name -> selectedPokemon = name },
            modifier = Modifier.width(ListPaneWidth).fillMaxHeight(),
        )
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val pokemonName = selectedPokemon
            if (pokemonName != null) {
                DetailsPaneRoute(pokemonName = pokemonName, modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Hello, Pokémon!", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
