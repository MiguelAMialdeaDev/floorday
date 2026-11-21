package studio.mialdea.floorday.buildlogic

/** Build rules without Gradle dependencies, so they can be tested with JUnit. */
object FloordayBuild {
    /** Base package. If ADR-0000 changes the name, only this constant changes. */
    const val BASE_PACKAGE = "studio.mialdea.floorday"
    const val APPLICATION_ID = BASE_PACKAGE

    /** JDK that compiles and runs the tests. Robolectric 4.18 requires 21 with SDK 36 or higher. */
    const val JDK_TOOLCHAIN = 21

    /**
     * Modules with an extra `jvm()` target used only by tests: database tests need the bundled
     * SQLite natives of the host JVM, and the modules they depend on must publish a JVM variant
     * too. There is no production JVM code.
     */
    private val JVM_TEST_MODULES: Set<String> = setOf(":core:domain", ":core:data", ":core:testing")

    fun hasJvmTestTarget(projectPath: String): Boolean = projectPath in JVM_TEST_MODULES

    /** Maximum versionCode accepted by Google Play. */
    private const val MAX_VERSION_CODE = 2_100_000_000

    /** ":core:domain" -> "studio.mialdea.floorday.core.domain". */
    fun namespaceFor(projectPath: String): String {
        val segments = projectPath.split(':').filter { it.isNotBlank() }
        require(segments.isNotEmpty()) { "El proyecto raíz no tiene namespace propio" }
        return (listOf(BASE_PACKAGE) + segments.map { it.replace('-', '_').lowercase() })
            .joinToString(".")
    }

    /** versionCode from the output of `git rev-list --count HEAD`. Without valid data, 1. */
    fun versionCodeFrom(gitCommitCount: String?): Int =
        gitCommitCount?.trim()?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?.coerceAtMost(MAX_VERSION_CODE)
            ?: 1
}
