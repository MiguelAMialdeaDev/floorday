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
 * floorday.android.application: AGP 9 con Kotlin integrado (sin kotlin-android), Compose compiler,
 * firma desde ~/.gradle/gradle.properties con fallback a debug, R8 y versionCode de release desde git.
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
                    versionCode = 1 // el de release se fija por variante (androidComponents, abajo)
                    versionName = appVersionName
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                // Con Kotlin integrado, el jvmTarget de Kotlin sale de targetCompatibility.
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
                    // Convive en el móvil con la versión de Play (firma distinta).
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
                    // Sin claves: firma debug para que assembleRelease funcione en el CI de PR.
                    // bundleRelease (lo único que se sube a Play) lo bloquea verifyReleaseSigning.
                    signingConfig = signingConfigs.getByName(if (release != null) "release" else "debug")
                }

                // Idioma por app (locales_config): sin split de idioma.
                bundle.language.enableSplit = false
                packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

                lint.abortOnError = true
                lint.checkDependencies = true
                // compileSdk 37 con targetSdk 36 es deliberado (exigencia de Play desde el 31-ago-2026).
                lint.disable += "OldTargetApi"
            }

            extensions.configure<ApplicationAndroidComponentsExtension> {
                // Solo release: debug se queda en 1 y no cambia con cada commit.
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
