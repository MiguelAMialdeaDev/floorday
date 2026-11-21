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
            // The shared repository contract (contract/) is a set of kotlin.test tests.
            api(libs.kotlin.test)
        }
        // On JVM targets kotlin.test.Test is only an expect until a framework is chosen: JUnit 4.
        androidMain.dependencies { api(libs.kotlin.test.junit) }
        jvmMain.dependencies { api(libs.kotlin.test.junit) }
    }
}
