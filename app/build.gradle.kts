plugins {
    alias(libs.plugins.pokedex.android.application)
    alias(libs.plugins.pokedex.android.application.compose)
    alias(libs.plugins.pokedex.android.hilt)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.ankitt.pokedex"

    defaultConfig {
        applicationId = "com.ankitt.pokedex"
    }

    // Release signing comes entirely from environment variables so no secret ever
    // touches the repo or gradle.properties. CI (see .github/workflows/release.yml)
    // exports these; a local release build must export them in the shell first.
    // Debug/CI builds without them still succeed - see the `signingConfigs.release`
    // block below - but the resulting APK/AAB is signed with the debug key and must
    // not be shipped.
    val releaseKeystorePath = System.getenv("POKEDEX_RELEASE_KEYSTORE_PATH")
    val hasReleaseSigningConfig = !releaseKeystorePath.isNullOrBlank()

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = System.getenv("POKEDEX_RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("POKEDEX_RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("POKEDEX_RELEASE_KEY_PASSWORD")
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (hasReleaseSigningConfig) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
}

dependencies {
    implementation(projects.feature.home)
    implementation(projects.feature.details)
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.core.network)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.window)

    // Overrides Compose's transitive 1.0.1 constraint - see the version catalog comment.
    implementation(libs.androidx.graphics.path)

    baselineProfile(projects.baselineprofile)
}

baselineProfile {
    automaticGenerationDuringBuild = false
}
