package com.ankitt.pokedex.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Top half of a circle: tips sit level with the bottom edge (left tip at the box's left
// edge, right tip at its right edge), curving up and over.
private const val ARC_START_ANGLE = 180f
private const val ARC_SWEEP_ANGLE = 180f

/**
 * A semicircle is exactly twice as wide as it is tall (height = radius, width = 2 * radius).
 * Locking the box to this ratio - rather than trusting the caller to hand-pick a matching
 * height - is what keeps the arc a true circular curve instead of a squashed/stretched oval,
 * at any width the caller gives it (a fixed dp, `fillMaxWidth()`, a fraction of the screen...).
 */
private const val ARC_ASPECT_RATIO = 2f

/**
 * A Pokemon GO-style CP gauge: a semicircular arc track with a colored sweep showing
 * `cp / maxCp`, and the CP value centered inside it. Purely presentational - takes plain
 * numbers, not a domain model, like every other shared component in this module. Give it a
 * width (e.g. matching the Pokemon image above it); its height follows automatically.
 */
@Composable
fun PokedexCpArc(
    cp: Int,
    modifier: Modifier = Modifier,
    maxCp: Int = 400,
    strokeWidth: Dp = 5.dp,
) {
    val progress = (cp.toFloat() / maxCp).coerceIn(0f, 1f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.aspectRatio(ARC_ASPECT_RATIO, matchHeightConstraintsFirst = false),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            // A true circle needs an equal-sided bounding box; deriving its diameter from the
            // canvas width alone (never its height) is what makes this robust to any screen
            // size - the aspect ratio above already guarantees height = width / 2 for us.
            val diameter = size.width - strokeWidthPx
            val arcTopLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = ARC_START_ANGLE,
                sweepAngle = ARC_SWEEP_ANGLE,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = ARC_START_ANGLE,
                sweepAngle = ARC_SWEEP_ANGLE * progress,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "CP", style = MaterialTheme.typography.labelMedium)
            Text(text = cp.toString(), style = MaterialTheme.typography.headlineSmall)
        }
    }
}
