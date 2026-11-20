// Plugins externos cargados una sola vez en el classloader raíz. build-logic los usa como compileOnly.
// KSP y Room 3 se añaden en el PR de Room; detekt y Kover, en el de calidad.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}
