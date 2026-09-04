plugins {
    `kotlin-dsl`
}

group = "com.ankitt.pokedex.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
}

// Registers each convention plugin under a stable id so modules can apply it
// with `plugins { id("pokedex.android.feature") }` instead of a fully-qualified class name.
gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "pokedex.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "pokedex.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "pokedex.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "pokedex.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "pokedex.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "pokedex.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("spotless") {
            id = "pokedex.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
    }
}
