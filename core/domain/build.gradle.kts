plugins {
    id("floorday.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Se declara ya para validar que resuelve en android, iosArm64 e iosSimulatorArm64.
            implementation(libs.kotlinx.datetime)
        }
    }
}
