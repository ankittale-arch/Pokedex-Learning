import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9's Kotlin support is built in - applying org.jetbrains.kotlin.android
            // on top of it is an error, not just redundant.
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            extensions.getByType<KotlinAndroidProjectExtension>().apply {
                configureKotlinAndroid(this)
            }
        }
    }
}
