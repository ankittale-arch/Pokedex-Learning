package com.ankitt.pokedex.feature.details

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ankitt.pokedex.core.common.capitalizeWords
import com.ankitt.pokedex.core.designsystem.component.NetworkRetryBanner
import com.ankitt.pokedex.core.designsystem.component.PokedexAsyncImage
import com.ankitt.pokedex.core.designsystem.component.PokedexCpArc
import com.ankitt.pokedex.core.designsystem.component.PokedexLoadingIndicator
import com.ankitt.pokedex.core.designsystem.theme.PokedexTheme
import com.ankitt.pokedex.core.model.Pokemon
import com.ankitt.pokedex.core.model.PokemonStat
import com.ankitt.pokedex.core.model.TypeEffectiveness
import com.ankitt.pokedex.core.model.calculateCp
import com.ankitt.pokedex.core.model.isEmpty
import com.ankitt.pokedex.core.preview.PokemonPreviewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DetailsRoute(
    pokemonName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    pagerViewModel: PokemonPagerViewModel = hiltViewModel(),
) {
    val pokemonNames by pagerViewModel.pokemonNames.collectAsState()

    // The dark scrim behind the status bar (see DetailsTopBar) needs light/white system icons
    // regardless of the device's own light/dark theme setting, or they'd read low-contrast
    // against it - restored to whatever Home had set once this screen goes away.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val wasLightStatusBars = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            if (wasLightStatusBars != null) controller?.isAppearanceLightStatusBars = wasLightStatusBars
        }
    }

    // The currently visible page's own pastel background, reported up by PokemonDetails below -
    // the top/bottom scrims darken this instead of using a fixed color, so they read as a deeper
    // shade of that same Pokemon's color rather than an unrelated black overlay. Null until the
    // first page reports in, so the scrims fall back to the theme's own surface tone.
    var currentPageColor by remember { mutableStateOf<Color?>(null) }
    val scrimBase = currentPageColor ?: MaterialTheme.colorScheme.surface
    // Blended most of the way to black so it's dark enough for the white status/nav bar icons
    // and back arrow to stay legible, while the hue still visibly comes from the page's own color.
    val scrimColor = lerp(scrimBase, Color.Black, 0.55f)

    Box(modifier = modifier.fillMaxSize()) {
        if (pokemonNames.isEmpty()) {
            // The Home list hasn't loaded into Room yet (e.g. deep link) - fall back to a
            // single, non-swipeable page for just the Pokemon that was navigated to.
            DetailsPage(
                pokemonName = pokemonName,
                isCurrentPage = true,
                onBackgroundColorChanged = { currentPageColor = it },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val initialPage = pokemonNames.indexOf(pokemonName).coerceAtLeast(0)
            val pagerState = rememberPagerState(initialPage = initialPage) { pokemonNames.size }

            // The pager only ever changes which page is visible - it never touches the nav
            // back stack, so the system back button/gesture and the arrow above both still
            // just pop this destination and return to Home, no matter which page is showing.
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                DetailsPage(
                    pokemonName = pokemonNames[page],
                    isCurrentPage = page == pagerState.currentPage,
                    onBackgroundColorChanged = { currentPageColor = it },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Sits above every page's content regardless of that page's own loading/error/success
        // state, so the back arrow is always reachable. Content still reaches the true top
        // edge and scrolls up behind it - this scrim just fades it out as it passes underneath
        // instead of colliding with the system status bar icons.
        DetailsTopBar(
            onBackClick = onBackClick,
            scrimColor = scrimColor,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Mirrors the top scrim at the bottom, over the system navigation bar - content scrolls
        // all the way to the true bottom edge and fades out under this instead of stopping short.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, scrimColor.copy(alpha = 0.85f)),
                    ),
                ),
        )
    }
}

/** A gradient scrim, not a solid toolbar - it fades from [scrimColor] (a darkened version of the
 * current page's own pastel background) at the true top edge down to fully transparent, so the
 * status bar icons and back arrow read clearly in front while the page's own content fades out as
 * it scrolls up behind them instead of colliding with the icons. The arrow is always drawn in
 * white - [scrimColor] is blended dark enough in both themes that a theme-following `onSurface`
 * tint would lose contrast against it. */
@Composable
private fun DetailsTopBar(onBackClick: () -> Unit, scrimColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 56.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(scrimColor.copy(alpha = 0.85f), Color.Transparent),
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun DetailsPage(
    pokemonName: String,
    isCurrentPage: Boolean,
    onBackgroundColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(key = pokemonName),
) {
    LaunchedEffect(pokemonName) { viewModel.loadPokemon(pokemonName) }
    val uiState by viewModel.uiState.collectAsState()
    DetailsScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        isCurrentPage = isCurrentPage,
        onBackgroundColorChanged = onBackgroundColorChanged,
        modifier = modifier,
    )
}

/** The detail pane of a tablet's side-by-side list-detail layout (see `PokedexListDetailScreen`
 * in `app`) - the same content a full-screen [DetailsRoute] page shows, minus the pager, back
 * arrow, and top/bottom scrims, since the list pane sitting next to it is the only "back" a
 * permanently visible two-pane layout needs. */
@Composable
fun DetailsPaneRoute(
    pokemonName: String,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(key = pokemonName),
) {
    LaunchedEffect(pokemonName) { viewModel.loadPokemon(pokemonName) }
    val uiState by viewModel.uiState.collectAsState()
    DetailsScreen(uiState = uiState, onRetry = viewModel::retry, modifier = modifier)
}

@Composable
internal fun DetailsScreen(
    uiState: DetailsUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentPage: Boolean = true,
    onBackgroundColorChanged: (Color) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is DetailsUiState.Loading -> PokedexLoadingIndicator(Modifier.fillMaxSize())
            is DetailsUiState.Success -> PokemonDetails(
                pokemon = uiState.pokemon,
                isCurrentPage = isCurrentPage,
                onBackgroundColorChanged = onBackgroundColorChanged,
                modifier = Modifier.fillMaxSize(),
            )
            is DetailsUiState.Error -> Unit // nothing cached to show - the banner below covers it
        }

        // Same component, same bottom placement, on every screen in the app - a failed
        // request never blocks out data that's already on screen.
        if (uiState is DetailsUiState.Error) {
            NetworkRetryBanner(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/** Blends most of the way to the theme's own background tone, so any extracted color - however
 * saturated the Pokemon's own sprite is - reads as a soft, solid pastel rather than a vivid or
 * muddy background. Blends toward white in light theme (a light pastel) and toward the dark
 * color scheme's surface in dark theme (a dark, muted pastel), so the result always matches the
 * current system theme instead of staying light-pastel regardless of it. */
@Composable
private fun Color.toPastel(): Color {
    val blendTarget = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color.White
    return lerp(this, blendTarget, 0.7f)
}

@Composable
private fun PokemonDetails(
    pokemon: Pokemon,
    isCurrentPage: Boolean,
    onBackgroundColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Re-extracted per Pokemon (keyed on id) so swiping the pager to a different page doesn't
    // keep showing the previous one's color while the new image loads.
    var dominantColor by remember(pokemon.id) { mutableStateOf<Color?>(null) }
    val scope = rememberCoroutineScope()
    val backgroundColor by animateColorAsState(
        targetValue = dominantColor?.toPastel() ?: MaterialTheme.colorScheme.surface,
        label = "PokemonBackgroundColor",
    )

    // Only the page currently visible in the pager drives the shared top/bottom scrim color -
    // otherwise a neighboring page finishing its own color extraction off-screen would yank the
    // scrim away from the page the user is actually looking at.
    if (isCurrentPage) {
        SideEffect { onBackgroundColorChanged(backgroundColor) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Painted here, before any inset/padding below, so it fills the entire box - all
            // the way behind the transparent status bar and the floating back arrow above it -
            // regardless of how narrow the content column below ends up on a wide screen.
            .background(backgroundColor),
    ) {
        // Capped and centered on wide screens (tablets) so stat bars, chips, and text don't
        // stretch edge-to-edge into an unreadably wide single column - on a phone-width screen
        // this max width never kicks in and fillMaxWidth behaves exactly as before.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Same 200dp width as the image below, so the arc's tips line up with its left/right
            // edges - its height follows automatically to keep the curve a true circle. Derived
            // fresh from this page's own `pokemon` every recomposition, so swiping the pager to a
            // different Pokemon always shows that page's own CP - never a stale one.
            PokedexCpArc(cp = pokemon.calculateCp(), modifier = Modifier.width(200.dp))
            PokedexAsyncImage(
                imageUrl = pokemon.imageUrl,
                contentDescription = pokemon.name,
                containerColor = Color.Transparent,
                onImageLoaded = { bitmap ->
                    scope.launch {
                        val color = withContext(Dispatchers.Default) { extractDominantColor(bitmap) }
                        dominantColor = color
                    }
                },
                modifier = Modifier.size(200.dp),
            )
            Text(text = "#${pokemon.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = pokemon.name.capitalizeWords(), style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pokemon.types.forEach { type -> PokemonTypeChip(type) }
            }
            if (pokemon.stats.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Base stats",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                PokemonStats(stats = pokemon.stats, modifier = Modifier.fillMaxWidth())
            }
            if (!pokemon.typeEffectiveness.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Type effectiveness",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                TypeEffectivenessSection(
                    effectiveness = pokemon.typeEffectiveness,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Same 112dp label column as [PokemonStatRow], so this section's labels line up with the
 * stats above it too - one consistent alignment for the whole screen. */
@Composable
private fun TypeEffectivenessSection(effectiveness: TypeEffectiveness, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (effectiveness.weakTo.isNotEmpty()) {
            TypeEffectivenessRow(label = "Weak to", types = effectiveness.weakTo)
        }
        if (effectiveness.resistantTo.isNotEmpty()) {
            TypeEffectivenessRow(label = "Resistant to", types = effectiveness.resistantTo)
        }
        if (effectiveness.immuneTo.isNotEmpty()) {
            TypeEffectivenessRow(label = "Immune to", types = effectiveness.immuneTo)
        }
    }
}

@Composable
private fun TypeEffectivenessRow(label: String, types: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .width(112.dp)
                .padding(top = 6.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            types.forEach { type -> PokemonTypeChip(type) }
        }
    }
}

/** The pastel background behind these chips is arbitrary and per-Pokemon - light and colorful
 * in light theme, dark and muted in dark theme - so a plain outlined/transparent chip (the
 * AssistChip default) can end up low-contrast against it either way. A solid `surface` container
 * instead reads as its own small card - near-white in light theme, dark gray in dark theme -
 * legible against any background hue in both themes. */
@Composable
private fun PokemonTypeChip(type: String, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = {},
        label = { Text(type.capitalizeWords()) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = null,
        modifier = modifier,
    )
}

/** Every row shares the same label width and value width, so labels, bars, and numbers
 * across all stats line up into three clean columns regardless of label text length. */
@Composable
private fun PokemonStats(stats: List<PokemonStat>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stats.forEach { stat -> PokemonStatRow(stat) }
    }
}

@Composable
private fun PokemonStatRow(stat: PokemonStat, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stat.name.capitalizeWords(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(112.dp),
        )
        LinearProgressIndicator(
            progress = { (stat.value / MAX_STAT_VALUE).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
        Text(
            text = stat.value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(36.dp)
                .padding(start = 8.dp),
        )
    }
}

/** Base stats rarely exceed this in practice - used only to normalize the bar fill, not a hard cap. */
private const val MAX_STAT_VALUE = 200f

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview
@Composable
private fun DetailsScreenPreview() {
    PokedexTheme {
        DetailsScreen(uiState = DetailsUiState.Success(PokemonPreviewData.bulbasaur), onRetry = {})
    }
}
