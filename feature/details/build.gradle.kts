plugins {
    id("pokedex.android.feature")
}

android {
    namespace = "com.ankitt.pokedex.feature.details"
}

dependencies {
    implementation(libs.androidx.palette.ktx)

    testImplementation(projects.core.test)
}
