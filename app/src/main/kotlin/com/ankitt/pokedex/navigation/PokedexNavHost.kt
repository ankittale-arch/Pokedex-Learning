package com.ankitt.pokedex.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.ankitt.pokedex.core.navigation.PokedexRoute
import com.ankitt.pokedex.feature.details.DetailsPaneRoute
import com.ankitt.pokedex.feature.details.DetailsRoute
import com.ankitt.pokedex.feature.home.HomeRoute

/** Below this width, Home pushes Details on top of it (see [PokedexRoute]) as its own
 * destination - a phone screen isn't wide enough to show both at once. At and above it (small
 * and large tablets, and phones in landscape), [PokedexListDetailScreen] shows them side by
 * side permanently instead, matching Material's guidance for list-detail layouts at the medium
 * window size class and up. A vertical, separating hinge (a Fold-style device opened into book
 * posture) also qualifies regardless of width - see [PokedexNavHost]. */
private const val LIST_DETAIL_MIN_WIDTH_DP = 600

/** `app` is the only module that knows about every feature - it wires their graphs together. */
@Composable
fun PokedexNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val foldingFeature = rememberFoldingFeature()
    val isBookPosture = foldingFeature?.isSeparating == true &&
        foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL
    val isTabletopPosture = foldingFeature?.isSeparating == true &&
        foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL

    when {
        // Tabletop (hinge running left-to-right, device propped up like a tiny laptop): the
        // screen splits into a near/far half rather than a left/right one, so the side-by-side
        // layout below doesn't fit here - fall through to the normal single-pane flow instead.
        isTabletopPosture -> PokedexSinglePaneNavHost(navController, modifier)
        // Book posture (hinge running top-to-bottom, e.g. a Fold opened partway) or a screen
        // wide enough to show both panes at once (unfolded, a tablet, or a phone in landscape).
        isBookPosture || LocalConfiguration.current.screenWidthDp >= LIST_DETAIL_MIN_WIDTH_DP ->
            PokedexListDetailScreen(modifier = modifier, hinge = foldingFeature.takeIf { isBookPosture })
        else -> PokedexSinglePaneNavHost(navController, modifier)
    }
}

/** The plain phone flow: Home pushes Details on top of it as its own back stack entry. */
@Composable
private fun PokedexSinglePaneNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
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

/** The list pane's own fixed width when there's no hinge to split at - wide enough for
 * [HomeRoute]'s grid to lay out at least two columns, while leaving the rest of the screen
 * (however wide) to the detail pane next to it. */
private val ListPaneWidth = 340.dp

/** List and detail side by side, both always on screen - selecting a Pokemon in the list just
 * swaps what the detail pane shows next to it, it never navigates away. Nothing selected yet
 * shows a plain placeholder in the detail pane instead of leaving it empty.
 *
 * [hinge] is the device's fold, when book posture put it at the split point instead - that way
 * neither pane is ever drawn underneath the seam. */
@Composable
private fun PokedexListDetailScreen(modifier: Modifier = Modifier, hinge: FoldingFeature? = null) {
    var selectedPokemon by rememberSaveable { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    val listPaneWidth = hinge?.let { with(density) { it.bounds.left.toDp() } } ?: ListPaneWidth

    Row(modifier = modifier.fillMaxSize()) {
        HomeRoute(
            onPokemonClick = { name -> selectedPokemon = name },
            modifier = Modifier.width(listPaneWidth).fillMaxHeight(),
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

/** The enclosing activity's current hinge, or null on a device with no fold (or one that hasn't
 * reported a [FoldingFeature] yet, or isn't hosted in an [Activity] at all - e.g. a Preview).
 * Tracks [WindowInfoTracker] live, so this updates as the device folds and unfolds without
 * needing the activity to be recreated. */
@Composable
private fun rememberFoldingFeature(): FoldingFeature? {
    val activity = LocalContext.current.findActivity() ?: return null
    var foldingFeature by remember { mutableStateOf<FoldingFeature?>(null) }
    LaunchedEffect(activity) {
        WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).collect { layoutInfo ->
            foldingFeature = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
        }
    }
    return foldingFeature
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
