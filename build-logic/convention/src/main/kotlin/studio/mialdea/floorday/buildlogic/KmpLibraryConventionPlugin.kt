package studio.mialdea.floorday.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/** floorday.kmp.library: KMP + Android KMP library + iosArm64/iosSimulatorArm64 + JDK 21/bytecode 17 + calidad. */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-multiplatform"))
            pluginManager.apply(libs.pluginId("android-kotlin-multiplatform-library"))
            pluginManager.apply(libs.pluginId("floorday-quality"))
            configureKotlinMultiplatform()
        }
    }
}
