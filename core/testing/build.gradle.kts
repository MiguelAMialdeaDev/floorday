plugins {
    id("floorday.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
        }
    }
}
