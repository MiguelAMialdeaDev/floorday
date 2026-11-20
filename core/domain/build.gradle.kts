plugins {
    id("floorday.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: the public models and contracts expose kotlinx-datetime and Flow types.
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            // FakeClock, FakeTimeZoneProvider and builders. Not a cycle: testing depends on main.
            implementation(project(":core:testing"))
        }
    }
}
