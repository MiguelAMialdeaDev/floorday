plugins {
    id("floorday.kmp.library")
}

// Solo se consume desde configuraciones de test (commonTest de los demás módulos).
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.kotlinx.coroutines.test)
            api(libs.turbine)
        }
    }
}
