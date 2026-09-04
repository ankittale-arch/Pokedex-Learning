plugins {
    alias(libs.plugins.pokedex.android.library)
    alias(libs.plugins.pokedex.android.hilt)
}

android {
    namespace = "com.ankitt.pokedex.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
}
