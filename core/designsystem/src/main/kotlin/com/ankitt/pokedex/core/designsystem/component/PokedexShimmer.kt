package com.ankitt.pokedex.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** A loading placeholder: a base tint with a soft highlight continuously sweeping across it. */
@Composable
fun PokedexShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val sweep by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
        ),
        label = "shimmerSweep",
    )
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .background(baseColor)
            .drawWithContent {
                drawContent()
                val bandWidth = size.width * 0.3f
                val centerX = size.width * sweep
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, highlightColor, Color.Transparent),
                        start = Offset(centerX - bandWidth, 0f),
                        end = Offset(centerX + bandWidth, 0f),
                    ),
                )
            },
    )
}
