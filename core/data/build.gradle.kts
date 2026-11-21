// floorday.room brings floorday.kmp.library, KSP per target, Room 3 and sqlite-bundled.
plugins {
    id("floorday.room")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            // api: DataPlatform exposes Storage<Preferences> to the platform implementations.
            api(libs.datastore.preferences.core)
        }
    }
}
