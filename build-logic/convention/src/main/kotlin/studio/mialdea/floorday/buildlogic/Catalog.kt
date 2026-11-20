package studio.mialdea.floorday.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { missing("library", alias) }

internal fun VersionCatalog.stringVersion(alias: String): String =
    findVersion(alias).orElseThrow { missing("version", alias) }.requiredVersion

internal fun VersionCatalog.intVersion(alias: String): Int = stringVersion(alias).toInt()

internal fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).orElseThrow { missing("plugin", alias) }.get().pluginId

private fun missing(kind: String, alias: String) =
    IllegalStateException("Falta '$alias' ($kind) en gradle/libs.versions.toml")
