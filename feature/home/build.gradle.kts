plugins {
    id("pokedex.android.feature")
}

android {
    namespace = "com.ankitt.pokedex.feature.home"
}

dependencies {
    testImplementation(projects.core.test)
}
