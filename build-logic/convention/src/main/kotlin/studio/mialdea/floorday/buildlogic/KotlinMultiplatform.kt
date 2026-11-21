package studio.mialdea.floorday.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    val catalog = libs
    val moduleNamespace = FloordayBuild.namespaceFor(path)

    extensions.configure<KotlinMultiplatformExtension> {
        // Toolchain 21 to compile and run tests; bytecode 17 on the Android target.
        jvmToolchain(FloordayBuild.JDK_TOOLCHAIN)

        // No iosX64: there are no Intel Macs in the plan.
        iosArm64()
        iosSimulatorArm64()

        if (FloordayBuild.hasJvmTestTarget(path)) {
            jvm {
                compilerOptions.jvmTarget.set(JvmTarget.fromTarget(catalog.stringVersion("jvmTarget")))
            }
        }

        // The Android target is the same object as the `kotlin { android {} }` block.
        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            namespace = moduleNamespace
            compileSdk = catalog.intVersion("android-compileSdk")
            minSdk = catalog.intVersion("android-minSdk")
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(catalog.stringVersion("jvmTarget")))
            // commonTest runs on the JVM inside androidHostTest. Modules must NOT
            // call withHostTest again: AGP fails if the compilation is created twice.
            withHostTest {}
        }

        sourceSets.getByName("commonMain").dependencies {
            implementation(catalog.library("kotlinx-coroutines-core"))
        }
        sourceSets.getByName("commonTest").dependencies {
            implementation(catalog.library("kotlin-test"))
            implementation(catalog.library("kotlinx-coroutines-test"))
            implementation(catalog.library("turbine"))
        }
        // Without this, kotlin.test.Test may end up without an actual on the external Android target.
        sourceSets.matching { it.name == "androidHostTest" }.configureEach {
            dependencies { implementation(catalog.library("kotlin-test-junit")) }
        }
    }
}
