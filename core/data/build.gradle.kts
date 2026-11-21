// floorday.room brings floorday.kmp.library, KSP per target, Room 3 and sqlite-bundled.
plugins {
    id("floorday.room")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
        }
    }
}
