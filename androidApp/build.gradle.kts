plugins {
    id("floorday.android.application")
}

// namespace, applicationId, versions, .debug suffix and signing come from floorday.android.application.
android {
    defaultConfig {
        manifestPlaceholders["appLabel"] = "Floorday"
    }
    buildTypes {
        getByName("debug") {
            manifestPlaceholders["appLabel"] = "Floorday debug"
        }
    }
}

dependencies {
    implementation(project(":shared"))
    // AndroidDataPlatform (androidMain of :core:data) is built here, where the Context lives.
    implementation(project(":core:data"))
    // androidPlatformModule implements the :core:domain/platform interfaces.
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    // Debug only: the seeder reads seed.json with JsonElement (no serialization plugin needed).
    debugImplementation(libs.kotlinx.serialization.json)
    // In-memory repositories, FakeClock and kotlin.test for the seeder tests.
    testImplementation(project(":core:testing"))
}
