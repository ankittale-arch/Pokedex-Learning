plugins {
    alias(libs.plugins.pokedex.android.library)
}

android {
    namespace = "com.ankitt.pokedex.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
