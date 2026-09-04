package com.ankitt.pokedex.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Gives convention plugin code access to the root `gradle/libs.versions.toml` catalog.
 *
 * Deliberately namespaced (not top-level in the default package): this plugin jar ends up
 * on every consuming project's script classpath, and an unqualified `Project.libs` here
 * would shadow Gradle's own generated type-safe `libs` accessor in every build.gradle.kts
 * with this plain [VersionCatalog] (which has no `.androidx.foo.bar`-style accessors).
 */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
