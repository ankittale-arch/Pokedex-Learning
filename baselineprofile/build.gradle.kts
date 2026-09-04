import org.gradle.api.JavaVersion

plugins {
    // AGP 9's Kotlin support is built in - no separate org.jetbrains.kotlin.android plugin needed.
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.ankitt.pokedex.baselineprofile"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":app"
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

baselineProfile {
    // Run manually via `./gradlew :baselineprofile:generateBaselineProfile` on a connected device.
    useConnectedDevices = true
}
