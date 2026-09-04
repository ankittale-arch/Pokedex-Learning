package com.ankitt.pokedex.feature.details

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

/** The Pokemon's own dominant color, so its details page can be tinted to match it. */
fun extractDominantColor(bitmap: Bitmap): Color? {
    // Palette reads pixels directly, which HARDWARE bitmaps can never allow - fall back to a
    // software copy rather than crash if one ever reaches here.
    val readableBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        bitmap
    }
    val palette = Palette.from(readableBitmap).generate()
    val swatch = palette.dominantSwatch ?: palette.vibrantSwatch ?: palette.mutedSwatch
    return swatch?.let { Color(it.rgb) }
}
