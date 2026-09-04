package com.ankitt.pokedex.core.designsystem.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Loads [imageUrl] over the network, with a shimmering placeholder while loading.
 *
 * @param containerColor background behind the image itself - pass [Color.Transparent] when the
 *   caller wants to draw its own background behind the image (e.g. a color extracted from it).
 * @param onImageLoaded called with the decoded bitmap once loading succeeds - e.g. to extract a
 *   dominant color from it. Runs on the main thread; do any real work off of it.
 */
@Composable
fun PokedexAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onImageLoaded: (Bitmap) -> Unit = {},
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            // These sprites are tiny, and callers may need real pixel access (e.g. Palette) -
            // HARDWARE bitmaps can't provide that, so this trades a no-op perf optimization
            // for correctness everywhere this component is used.
            .allowHardware(false)
            .listener(onSuccess = { _, result -> onImageLoaded(result.drawable.toBitmapSafely()) })
            .build()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            loading = { PokedexShimmer(modifier = Modifier.fillMaxSize()) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Coil's success drawable is a plain [BitmapDrawable] for network images; drawn to a bitmap
 * manually for the rare non-bitmap case rather than requiring callers to handle both. */
private fun android.graphics.drawable.Drawable.toBitmapSafely(): Bitmap {
    (this as? BitmapDrawable)?.bitmap?.let { return it }
    val width = intrinsicWidth.coerceAtLeast(1)
    val height = intrinsicHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))
    return bitmap
}
