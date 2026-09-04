pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "PokeDex"

include(":app")

include(":core:model")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:common")
include(":core:navigation")
include(":core:designsystem")
include(":core:preview")
include(":core:test")

include(":feature:home")
include(":feature:details")

include(":baselineprofile")
