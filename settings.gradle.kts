pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Descarga el JDK 21 del toolchain si no está instalado.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "floorday"

// Gradle 9 falla si un proyecto incluido no tiene carpeta: cada include entra con su módulo.
include(":core:domain")
include(":core:data")
include(":core:testing")
include(":core:designsystem")
include(":feature:today")
include(":feature:history")
include(":feature:settings")
include(":shared")
