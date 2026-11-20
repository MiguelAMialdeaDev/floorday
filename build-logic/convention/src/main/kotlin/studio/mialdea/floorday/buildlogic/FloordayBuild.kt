package studio.mialdea.floorday.buildlogic

/** Reglas del build sin dependencias de Gradle, para poder testearlas con JUnit. */
object FloordayBuild {
    /** Paquete base. Si ADR-0000 cambia el nombre, solo se toca esta constante. */
    const val BASE_PACKAGE = "studio.mialdea.floorday"
    const val APPLICATION_ID = BASE_PACKAGE

    /** JDK que compila y ejecuta los tests. Robolectric 4.18 pide 21 con SDK 36 o superior. */
    const val JDK_TOOLCHAIN = 21

    /** Tope de versionCode que admite Google Play. */
    private const val MAX_VERSION_CODE = 2_100_000_000

    /** ":core:domain" -> "studio.mialdea.floorday.core.domain". */
    fun namespaceFor(projectPath: String): String {
        val segments = projectPath.split(':').filter { it.isNotBlank() }
        require(segments.isNotEmpty()) { "El proyecto raíz no tiene namespace propio" }
        return (listOf(BASE_PACKAGE) + segments.map { it.replace('-', '_').lowercase() })
            .joinToString(".")
    }

    /** versionCode a partir de la salida de `git rev-list --count HEAD`. Sin dato válido, 1. */
    fun versionCodeFrom(gitCommitCount: String?): Int =
        gitCommitCount?.trim()?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?.coerceAtMost(MAX_VERSION_CODE)
            ?: 1
}
