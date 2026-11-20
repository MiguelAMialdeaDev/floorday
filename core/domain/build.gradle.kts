plugins {
    id("floorday.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: los modelos públicos exponen DayOfWeek, LocalDate y LocalTime.
            api(libs.kotlinx.datetime)
        }
    }
}
