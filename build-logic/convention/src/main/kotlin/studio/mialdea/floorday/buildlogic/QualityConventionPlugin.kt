package studio.mialdea.floorday.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * floorday.quality: stub en S0 (guía del sprint, 2.7). detekt, Kover con `minLineCoverage`,
 * lint, `checkModuleGraph`, `verifyMergedManifest` y la comprobación de 16 KB llegan en S1
 * (issue "Detekt, Kover y lint"). Se registra ya para que los demás plugins lo apliquen.
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = Unit
}
