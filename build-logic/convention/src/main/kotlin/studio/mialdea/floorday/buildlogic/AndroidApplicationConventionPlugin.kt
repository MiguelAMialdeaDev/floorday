package studio.mialdea.floorday.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure

/**
 * floorday.android.application: AGP 9 with built-in Kotlin (no kotlin-android), Compose compiler,
 * signing from ~/.gradle/gradle.properties with debug fallback, R8 and release versionCode from git.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("android-application"))
            pluginManager.apply(libs.pluginId("compose-compiler"))
            pluginManager.apply(libs.pluginId("floorday-quality"))

            val catalog = libs
            val jvmTarget = JavaVersion.toVersion(catalog.stringVersion("jvmTarget"))

            val signing = ReleaseSigningResolver.resolve { key -> providers.gradleProperty(key).orNull }
            val release = (signing as? SigningResolution.Release)?.signing
            val missingSigningKeys = (signing as? SigningResolution.DebugFallback)?.missingKeys.orEmpty()
            val keystoreFile = release?.let { file(it.storeFile) }

            val gitVersionCode = providers.exec {
                commandLine("git", "rev-list", "--count", "HEAD")
                workingDir = rootDir
                isIgnoreExitValue = true
            }.standardOutput.asText.map(FloordayBuild::versionCodeFrom)
            val appVersionName = providers.gradleProperty("floorday.versionName").getOrElse("0.0.0")

            extensions.configure<JavaPluginExtension> {
                toolchain.languageVersion.set(JavaLanguageVersion.of(FloordayBuild.JDK_TOOLCHAIN))
            }

            extensions.configure<ApplicationExtension> {
                namespace = FloordayBuild.BASE_PACKAGE
                compileSdk = catalog.intVersion("android-compileSdk")

                defaultConfig.apply {
                    applicationId = FloordayBuild.APPLICATION_ID
                    minSdk = catalog.intVersion("android-minSdk")
                    targetSdk = catalog.intVersion("android-targetSdk")
                    versionCode = 1 // release is set per variant (androidComponents, below)
                    versionName = appVersionName
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                // With built-in Kotlin, Kotlin's jvmTarget comes from targetCompatibility.
                compileOptions.apply {
                    sourceCompatibility = jvmTarget
                    targetCompatibility = jvmTarget
                }

                buildFeatures.compose = true

                if (release != null) {
                    signingConfigs.create("release").apply {
                        storeFile = keystoreFile
                        storePassword = release.storePassword
                        keyAlias = release.keyAlias
                        keyPassword = release.keyPassword
                    }
                }

                buildTypes.getByName("debug").apply {
                    // Coexists on the phone with the Play version (different signature).
                    applicationIdSuffix = ".debug"
                    versionNameSuffix = "-debug"
                }
                buildTypes.getByName("release").apply {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                    // No keys: debug signing so assembleRelease works in PR CI.
                    // bundleRelease (the only thing uploaded to Play) is blocked by verifyReleaseSigning.
                    signingConfig = signingConfigs.getByName(if (release != null) "release" else "debug")
                }

                // Per-app language (locales_config): no language split.
                bundle.language.enableSplit = false
                packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

                lint.abortOnError = true
                lint.checkDependencies = true
                // compileSdk 37 with targetSdk 36 is deliberate (Play requirement since 31-Aug-2026).
                lint.disable += "OldTargetApi"
            }

            extensions.configure<ApplicationAndroidComponentsExtension> {
                // Release only: debug stays at 1 and does not change with every commit.
                onVariants(selector().withBuildType("release")) { variant ->
                    variant.outputs.forEach { output -> output.versionCode.set(gitVersionCode) }
                }
            }

            val verifyReleaseSigning = tasks.register("verifyReleaseSigning") {
                group = "verification"
                description = "Falla si el AAB de release no se puede firmar con la clave de subida."
                val versionCodeProvider = gitVersionCode
                doLast {
                    if (missingSigningKeys.isNotEmpty()) {
                        throw GradleException(
                            "Faltan en ~/.gradle/gradle.properties: " + missingSigningKeys.joinToString(),
                        )
                    }
                    if (keystoreFile == null || !keystoreFile.isFile) {
                        throw GradleException("No existe el keystore de subida: $keystoreFile")
                    }
                    if (versionCodeProvider.get() <= 1) {
                        throw GradleException(
                            "versionCode = 1: ¿clon sin historia? En CI usa actions/checkout con fetch-depth: 0.",
                        )
                    }
                }
            }
            tasks.named { it == "bundleRelease" }.configureEach { dependsOn(verifyReleaseSigning) }

            if (missingSigningKeys.isNotEmpty()) {
                tasks.named { it == "assembleRelease" }.configureEach {
                    doFirst {
                        logger.warn("Floorday: release firmado con la clave DEBUG. No sirve para subir a Play.")
                    }
                }
            }
        }
    }
}
