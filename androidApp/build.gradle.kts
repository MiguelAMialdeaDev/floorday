plugins {
    id("floorday.android.application")
}

// namespace, applicationId, versiones, sufijo .debug y firma vienen de floorday.android.application.
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
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
