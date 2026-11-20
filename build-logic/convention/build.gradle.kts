import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "studio.mialdea.floorday.buildlogic"

// Fixed bytecode, whatever the daemon's JDK is.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_17 }
}

dependencies {
    // compileOnly: the real versions are set by the root build.gradle.kts (apply false).
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)

    // No version: kotlin-dsl aligns it with the Kotlin embedded in Gradle.
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
        register("floordayKmpCompose") {
            id = libs.plugins.floorday.kmp.compose.get().pluginId
            implementationClass = "studio.mialdea.floorday.buildlogic.KmpComposeConventionPlugin"
        }
        register("floordayKmpFeature") {
            id = libs.plugins.floorday.kmp.feature.get().pluginId
            implementationClass = "studio.mialdea.floorday.buildlogic.KmpFeatureConventionPlugin"
        }
        register("floordayAndroidApplication") {
            id = libs.plugins.floorday.android.application.get().pluginId
            implementationClass = "studio.mialdea.floorday.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("floordayRoom") {
            id = libs.plugins.floorday.room.get().pluginId
            implementationClass = "studio.mialdea.floorday.buildlogic.RoomConventionPlugin"
        }
        register("floordayQuality") {
            id = libs.plugins.floorday.quality.get().pluginId
            implementationClass = "studio.mialdea.floorday.buildlogic.QualityConventionPlugin"
        }
    }
}
