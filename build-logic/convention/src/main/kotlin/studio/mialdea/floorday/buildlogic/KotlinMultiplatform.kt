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
        // Toolchain 21 para compilar y ejecutar tests; bytecode 17 en el target Android.
        jvmToolchain(FloordayBuild.JDK_TOOLCHAIN)

        // Sin iosX64: no hay Macs Intel en el plan.
        iosArm64()
        iosSimulatorArm64()

        // El target Android es el mismo objeto que el bloque `kotlin { android {} }`.
        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            namespace = moduleNamespace
            compileSdk = catalog.intVersion("android-compileSdk")
            minSdk = catalog.intVersion("android-minSdk")
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(catalog.stringVersion("jvmTarget")))
            // commonTest se ejecuta en la JVM dentro de androidHostTest. Los módulos NO deben
            // volver a llamar a withHostTest: AGP falla si la compilación se crea dos veces.
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
        // Sin esto, kotlin.test.Test puede quedarse sin actual en el target Android externo.
        sourceSets.matching { it.name == "androidHostTest" }.configureEach {
            dependencies { implementation(catalog.library("kotlin-test-junit")) }
        }
    }
}
