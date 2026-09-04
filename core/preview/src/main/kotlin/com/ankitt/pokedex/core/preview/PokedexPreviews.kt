package com.ankitt.pokedex.core.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** Renders a `@Preview` composable in both light and dark theme at once. */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class PokedexPreviews
