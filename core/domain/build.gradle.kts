plugins {
    id("floorday.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: los modelos y contratos públicos exponen tipos de kotlinx-datetime y Flow.
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            // FakeClock, FakeTimeZoneProvider y builders. No es un ciclo: testing depende de main.
            implementation(project(":core:testing"))
        }
    }
}
