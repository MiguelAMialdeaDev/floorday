plugins {
    id("floorday.kmp.compose")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "FloordayShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:designsystem"))
            implementation(project(":feature:today"))
            implementation(project(":feature:history"))
            implementation(project(":feature:settings"))

            // navigation3-ui (JetBrains) brings in as api androidx.navigation3:navigation3-runtime
            // (NavKey, entryProvider) and androidx.savedstate (SavedStateConfiguration).
            implementation(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.kotlinx.serialization.core)

            // api: initKoin exposes KoinApplication and Module, and the BOM must travel with koin-core.
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
