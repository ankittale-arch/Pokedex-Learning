import com.android.build.api.dsl.CommonExtension
import com.ankitt.pokedex.buildlogic.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Single source of truth for the Compose compiler setup, called by every
 * `pokedex.android.*.compose` convention plugin. Compose library versions are pinned
 * directly in the catalog (not via a BOM) so they stay in lockstep with the AGP/compileSdk
 * pair this project is built against.
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        buildFeatures.compose = true
    }

    dependencies {
        add("implementation", libs.findLibrary("androidx-compose-ui").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}
