plugins {
    alias(libs.plugins.pokedex.android.library)
    alias(libs.plugins.pokedex.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ankitt.pokedex.core.network"

    // Needed so providesOkHttpClient() below can gate request/response body logging on
    // BuildConfig.DEBUG - a release build must never log full API traffic to logcat.
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.retrofit.bom))
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
