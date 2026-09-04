plugins {
    alias(libs.plugins.pokedex.android.library)
    alias(libs.plugins.pokedex.android.hilt)
}

android {
    namespace = "com.ankitt.pokedex.core.database"
}

dependencies {
    implementation(projects.core.model)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
