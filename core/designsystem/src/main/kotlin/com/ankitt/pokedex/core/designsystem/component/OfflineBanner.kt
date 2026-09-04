package com.ankitt.pokedex.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A persistent "you're offline" indicator - unlike [NetworkRetryBanner], this isn't tied to any
 * one failed request. It reflects live device connectivity and disappears the instant the
 * connection comes back, on every screen, since it's rendered once at the app's root.
 */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.error,
    ) {
        Text(
            text = "No internet connection",
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        )
    }
}
