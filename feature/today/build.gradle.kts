plugins {
    id("floorday.kmp.feature")
}

kotlin {
    sourceSets {
        getByName("androidHostTest").dependencies {
            implementation(libs.junit4)
            implementation(libs.robolectric)
            implementation(libs.androidx.test.core)
        }
    }
}
