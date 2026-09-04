plugins {
    alias(libs.plugins.pokedex.android.library)
    alias(libs.plugins.pokedex.android.library.compose)
}

android {
    namespace = "com.ankitt.pokedex.core.designsystem"
}

dependencies {
    implementation(libs.coil.compose)
    // api: feature modules need the Icons.* objects themselves, not just this module's own use.
    api(libs.androidx.compose.material.icons.core)
}
