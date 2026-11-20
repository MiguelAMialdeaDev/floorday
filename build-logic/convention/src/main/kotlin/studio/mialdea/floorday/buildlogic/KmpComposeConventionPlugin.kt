package studio.mialdea.floorday.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** floorday.kmp.compose: floorday.kmp.library + Compose Multiplatform + resources that work on Android. */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("floorday-kmp-library"))
            pluginManager.apply(libs.pluginId("compose-multiplatform"))
            pluginManager.apply(libs.pluginId("compose-compiler"))

            val catalog = libs
            val moduleNamespace = FloordayBuild.namespaceFor(path)

            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                    // Without this, Compose resources fail at runtime with MissingResourceException.
                    androidResources { enable = true }
                    compilations.withType<KotlinMultiplatformAndroidHostTestCompilation>().configureEach {
                        isIncludeAndroidResources = true
                    }
                }

                sourceSets.getByName("commonMain").dependencies {
                    implementation(catalog.library("compose-runtime"))
                    implementation(catalog.library("compose-foundation"))
                    implementation(catalog.library("compose-ui"))
                    implementation(catalog.library("compose-material3"))
                    implementation(catalog.library("compose-components-resources"))
                    implementation(catalog.library("compose-ui-tooling-preview"))
                }
            }

            // Android Studio preview: ui-tooling only at runtime, never in the library's API.
            dependencies.add("androidRuntimeClasspath", catalog.library("compose-ui-tooling"))

            // Res in <namespace>.resources; it stays internal (publicResClass = false by default).
            extensions.configure<ComposeExtension> {
                extensions.configure<ResourcesExtension> {
                    packageOfResClass = "$moduleNamespace.resources"
                }
            }
        }
    }
}
