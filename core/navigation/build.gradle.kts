plugins {
    alias(libs.plugins.pokedex.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ankitt.pokedex.core.navigation"
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
