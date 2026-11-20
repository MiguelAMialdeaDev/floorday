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

/** floorday.kmp.compose: floorday.kmp.library + Compose Multiplatform + recursos que funcionan en Android. */
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
                    // Sin esto, las Compose resources fallan en runtime con MissingResourceException.
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

            // Preview de Android Studio: ui-tooling solo en runtime, nunca en la API de la librería.
            dependencies.add("androidRuntimeClasspath", catalog.library("compose-ui-tooling"))

            // Res en <namespace>.resources; sigue siendo internal (publicResClass = false por defecto).
            extensions.configure<ComposeExtension> {
                extensions.configure<ResourcesExtension> {
                    packageOfResClass = "$moduleNamespace.resources"
                }
            }
        }
    }
}
