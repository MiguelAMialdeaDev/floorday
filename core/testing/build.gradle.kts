plugins {
    id("floorday.kmp.library")
}

// Only consumed from test configurations (commonTest of the other modules).
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.kotlinx.coroutines.test)
            api(libs.turbine)
        }
    }
}
