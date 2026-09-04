package com.ankitt.pokedex.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Fixed brand palette rather than Material You dynamic color (`dynamicLightColorScheme`/
// `dynamicDarkColorScheme`) - a Pokedex should look like a Pokedex on every device, not get
// re-tinted to match the user's wallpaper.
private val LightColors = lightColorScheme(
    primary = PokedexRed,
    secondary = PokedexBlue,
    tertiary = PokedexYellow,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

private val DarkColors = darkColorScheme(
    primary = PokedexRedDark,
    secondary = PokedexBlue,
    tertiary = PokedexYellow,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
)

@Composable
fun PokedexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PokedexTypography,
        content = content,
    )
}
