import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Single source of truth for compileSdk/minSdk. Called by every `pokedex.android.*`
 * convention plugin so these numbers only live here.
 *
 * AGP 9's Kotlin support is built in - there's no separate `org.jetbrains.kotlin.android`
 * plugin to apply, so this only configures the Android extension; JVM target and compiler
 * args are configured separately via the [KotlinAndroidProjectExtension] overload below,
 * which AGP registers automatically once it detects Kotlin sources.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        compileSdk = 36
        defaultConfig.minSdk = 23
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }
}

internal fun Project.configureKotlinAndroid(
    extension: KotlinAndroidProjectExtension,
) {
    extension.apply {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            )
        }
    }
}
