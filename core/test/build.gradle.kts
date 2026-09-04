plugins {
    alias(libs.plugins.pokedex.android.library)
}

android {
    namespace = "com.ankitt.pokedex.core.test"
}

dependencies {
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.truth)
    api(libs.mockito.core)
    api(libs.mockito.kotlin)
}
