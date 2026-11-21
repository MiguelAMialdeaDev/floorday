package studio.mialdea.floorday.buildlogic

import androidx.room3.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

/**
 * floorday.room: floorday.kmp.library + KSP + Room 3 with the bundled SQLite driver.
 *
 * - The Room compiler is added to `ksp<Target>` of every non-common target, lazily, so a
 *   target declared later (the test-only `jvm()`) also gets its generated `actual`.
 * - The schema is exported to `<module>/schemas` and versioned in git.
 * - `-Xexpect-actual-classes`: `expect object <App>DatabaseConstructor` is an expect/actual
 *   class (Beta) and warns without it.
 * - JVM tests receive the schema folder in `floorday.schemaDir` for the migration test.
 *
 * The `ksp` and `room3` plugins must be on the root classpath (`apply false` in the root build).
 */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("floorday-kmp-library"))
            pluginManager.apply(libs.pluginId("ksp"))
            pluginManager.apply(libs.pluginId("room3"))

            val catalog = libs
            val schemaDir = layout.projectDirectory.dir("schemas")

            extensions.configure<RoomExtension> {
                schemaDirectory(schemaDir.asFile.path)
            }

            val roomCompiler = catalog.library("room3-compiler")
            extensions.configure<KotlinMultiplatformExtension> {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                sourceSets.getByName("commonMain").dependencies {
                    implementation(catalog.library("room3-runtime"))
                    implementation(catalog.library("sqlite-bundled"))
                }

                targets
                    .matching { it.platformType != KotlinPlatformType.common }
                    .configureEach {
                        val kspConfiguration = "ksp" + name.replaceFirstChar { it.uppercase() }
                        configurations
                            .matching { it.name == kspConfiguration }
                            .configureEach { project.dependencies.add(name, roomCompiler) }
                    }
            }

            val schemaPath = schemaDir.asFile.absolutePath
            tasks.withType<Test>().configureEach {
                inputs.dir(schemaDir).withPropertyName("roomSchemas")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
                    .optional()
                systemProperty("floorday.schemaDir", schemaPath)
            }
        }
    }
}
