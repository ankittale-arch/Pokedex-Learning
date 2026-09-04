package com.ankitt.pokedex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ankitt.pokedex.core.navigation.PokedexRoute
import com.ankitt.pokedex.feature.details.DetailsRoute
import com.ankitt.pokedex.feature.home.HomeRoute

/** `app` is the only module that knows about every feature - it wires their graphs together. */
@Composable
fun PokedexNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
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
