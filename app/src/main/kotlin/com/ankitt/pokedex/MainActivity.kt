package com.ankitt.pokedex

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.ankitt.pokedex.core.designsystem.component.OfflineBanner
import com.ankitt.pokedex.core.designsystem.theme.PokedexTheme
import com.ankitt.pokedex.navigation.PokedexNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No scrim at all - screens paint their own background all the way to the true
        // top/bottom edges. `auto` picks dark-on-light or light-on-dark status bar icons to
        // match the system's current dark/light setting, same as PokedexTheme below.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
        setContent {
            PokedexTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Edge-to-edge, full stop: NavHost fills the entire screen from the true
                        // top, so each screen's own background can reach it too - no shared
                        // inset here that would otherwise clip every screen's background short.
                        PokedexNavHost(
                            navController = rememberNavController(),
                            modifier = Modifier.fillMaxSize(),
                        )

                        val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
                        val isOnline by connectivityViewModel.isOnline.collectAsState()
                        if (!isOnline) {
                            OfflineBanner(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .statusBarsPadding(),
                            )
                        }
                    }
                }
            }
        }
    }
}
