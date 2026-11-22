plugins {
    id("floorday.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: MviViewModel extends ViewModel and CollectEffects reads LocalLifecycleOwner.
            api(libs.lifecycle.viewmodel.compose)
            api(libs.lifecycle.runtime.compose)
        }
        commonTest.dependencies {
            // runMainTest replaces Dispatchers.Main for the MviViewModel tests.
            implementation(project(":core:testing"))
        }
    }
}
