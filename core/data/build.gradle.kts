plugins {
    alias(libs.plugins.pokedex.android.library)
    alias(libs.plugins.pokedex.android.hilt)
}

android {
    namespace = "com.ankitt.pokedex.core.data"
}

dependencies {
    // Pokemon (core:model) is part of PokemonRepository's public surface, so it must be
    // `api`: consumers of this module need it on their own compile classpath too.
    api(projects.core.model)

    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.common)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.test)
    // HttpException/Response, for constructing a realistic API-error case in tests.
    testImplementation(platform(libs.retrofit.bom))
    testImplementation(libs.retrofit.core)
}
