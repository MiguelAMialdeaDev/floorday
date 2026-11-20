package studio.mialdea.floorday.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * floorday.quality: stub in S0 (sprint guide, 2.7). detekt, Kover with `minLineCoverage`,
 * lint, `checkModuleGraph`, `verifyMergedManifest` and the 16 KB check arrive in S1
 * (issue "Detekt, Kover y lint"). It is registered now so the other plugins apply it.
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = Unit
}
