package com.ankitt.pokedex.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ankitt.pokedex.core.common.capitalizeWords
import com.ankitt.pokedex.core.designsystem.component.NetworkRetryBanner
import com.ankitt.pokedex.core.designsystem.component.PokedexAsyncImage
import com.ankitt.pokedex.core.designsystem.component.PokedexLoadingIndicator
import com.ankitt.pokedex.core.designsystem.theme.PokedexTheme
import com.ankitt.pokedex.core.model.Pokemon
import com.ankitt.pokedex.core.preview.PokemonPreviewData

@Composable
fun HomeRoute(
    onPokemonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onPokemonClick = onPokemonClick,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onPokemonClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // No longer applied globally in MainActivity (Details needs its background to reach the
    // true top edge), so Home - which has no edge-to-edge background of its own - handles its
    // own top inset here.
    Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        when {
            uiState.pokemonList.isEmpty() && (uiState.isLoadingMore || uiState.isRefreshing) ->
                PokedexLoadingIndicator(Modifier.fillMaxSize())

            uiState.pokemonList.isNotEmpty() -> PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                PokemonGrid(
                    pokemonList = uiState.pokemonList,
                    isLoadingMore = uiState.isLoadingMore,
                    onPokemonClick = onPokemonClick,
                    onLoadMore = onLoadMore,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> Unit // empty, not loading, errored - the banner below covers it
        }

        // Same component, same bottom placement, on every screen in the app - a failed
        // request never blocks out data that's already on screen.
        uiState.errorMessage?.let { message ->
            NetworkRetryBanner(
                message = message,
                onRetry = onRetry,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemonList: List<Pokemon>,
    isLoadingMore: Boolean,
    onPokemonClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyStaggeredGridState()

    // Kick off the next page a few cells before the user actually hits the bottom.
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            pokemonList.isNotEmpty() && lastVisible >= pokemonList.size - 6
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    // Adaptive rather than a fixed count tied to the device's full screen width: on a tablet
    // this grid is often only handed half the screen (see PokedexListDetailScreen), so the
    // column count needs to react to the space actually available to it, not the whole display.
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 130.dp),
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
        items(items = pokemonList, key = { it.id }) { pokemon ->
            PokemonGridItem(pokemon = pokemon, onClick = { onPokemonClick(pokemon.name) })
        }
        if (isLoadingMore) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun PokemonGridItem(pokemon: Pokemon, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PokedexAsyncImage(
                imageUrl = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = pokemon.name.capitalizeWords(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    PokedexTheme {
        HomeScreen(
            uiState = HomeUiState(pokemonList = PokemonPreviewData.pokemonList),
            onPokemonClick = {},
            onRefresh = {},
            onLoadMore = {},
            onRetry = {},
        )
    }
}
