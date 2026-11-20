import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "studio.mialdea.floorday.buildlogic"

// Bytecode fijo, sea cual sea el JDK del daemon.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_17 }
}

dependencies {
    // compileOnly: las versiones reales las pone el build.gradle.kts raíz (apply false).
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)

    // Sin versión: kotlin-dsl la alinea con el Kotlin integrado en Gradle.
    testImplementation(kotlin("test-junit"))
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

tasks.validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
}

gradlePlugin {
    plugins {
        register("floordayKmpLibrary") {
            id = libs.plugins.floorday.kmp.library.get().pluginId
            implementationClass = "studio.mialdea.floorday.buildlogic.KmpLibraryConventionPlugin"
        }
        register("floordayQuality") {
            id = libs.plugins.floorday.quality.get().pluginId
            implementationClass = "studio.mialdea.floorday.buildlogic.QualityConventionPlugin"
        }
    }
}
