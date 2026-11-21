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
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
        // Database tests run here, never in androidHostTest: the bundled SQLite natives of
        // the Android AAR do not load on the host JVM.
        jvmTest.dependencies {
            implementation(libs.room3.testing)
            implementation(libs.junit4)
        }
    }
}
