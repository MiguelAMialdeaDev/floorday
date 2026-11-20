// floorday.room se aplica en el PR de Room 3.
plugins {
    id("floorday.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
        }
    }
}
