package com.ankitt.pokedex.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Not a real test: run on a device/emulator (`./gradlew :baselineprofile:pixel6Api34BenchmarkAndroidTest`
 * or via `:app`'s `baselineProfile` block, see `app/build.gradle.kts`) to record which classes/
 * methods execute during a critical user journey - here, just cold app startup. The recorded
 * profile ships inside the release APK/AAB so ART can AOT-compile that exact code path ahead of
 * time on the user's device instead of interpreting/JIT-compiling it on first launch, which is
 * what actually speeds up cold start. `automaticGenerationDuringBuild = false` on the app module
 * means this only regenerates when explicitly run, not on every release build.
 */
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(packageName = "com.ankitt.pokedex") {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5_000)
    }
}
