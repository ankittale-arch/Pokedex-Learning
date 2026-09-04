plugins {
    alias(libs.plugins.pokedex.android.library)
    alias(libs.plugins.pokedex.android.library.compose)
}

android {
    namespace = "com.ankitt.pokedex.core.preview"
}

dependencies {
    implementation(projects.core.model)
}
