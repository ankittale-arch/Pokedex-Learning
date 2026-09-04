import com.ankitt.pokedex.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Applied by every `feature:*` module. Bundles library + Compose + Hilt setup and the
 * dependencies every feature needs, so a feature's own build.gradle.kts only needs
 * `plugins { id("pokedex.android.feature") }` and a namespace.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("pokedex.android.library")
                apply("pokedex.android.library.compose")
                apply("pokedex.android.hilt")
            }

            dependencies {
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:navigation"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:preview"))
                add("implementation", project(":core:common"))
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            }
        }
    }
}
