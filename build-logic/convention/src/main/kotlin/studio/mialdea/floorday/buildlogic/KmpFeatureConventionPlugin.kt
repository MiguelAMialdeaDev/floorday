package studio.mialdea.floorday.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * floorday.kmp.feature: compose + ViewModel + Koin + core:domain y core:designsystem.
 * Sin Navigation 3: los features exponen `XxxRoute(callbacks)` y `:shared` monta el entryProvider.
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("floorday-kmp-compose"))

            val catalog = libs
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:domain"))
                    implementation(project(":core:designsystem"))

                    implementation(catalog.library("lifecycle-viewmodel-compose"))
                    implementation(catalog.library("lifecycle-runtime-compose"))

                    implementation(target.dependencies.platform(catalog.library("koin-bom")))
                    implementation(catalog.library("koin-compose-viewmodel"))
                }
                sourceSets.getByName("commonTest").dependencies {
                    implementation(project(":core:testing"))
                }
            }
        }
    }
}
