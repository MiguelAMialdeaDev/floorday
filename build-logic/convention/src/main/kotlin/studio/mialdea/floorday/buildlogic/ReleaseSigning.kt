package studio.mialdea.floorday.buildlogic

data class ReleaseSigning(
    val storeFile: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
)

sealed interface SigningResolution {
    data class Release(val signing: ReleaseSigning) : SigningResolution
    data class DebugFallback(val missingKeys: List<String>) : SigningResolution
}

/** Lee las 4 claves de ~/.gradle/gradle.properties (o -P). Un valor en blanco cuenta como ausente. */
object ReleaseSigningResolver {
    const val KEYSTORE_PATH = "FLOORDAY_KEYSTORE_PATH"
    const val KEYSTORE_PASSWORD = "FLOORDAY_KEYSTORE_PASSWORD"
    const val KEY_ALIAS = "FLOORDAY_KEY_ALIAS"
    const val KEY_PASSWORD = "FLOORDAY_KEY_PASSWORD"

    val KEYS = listOf(KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)

    fun resolve(read: (String) -> String?): SigningResolution {
        val values = KEYS.associateWith { key -> read(key)?.trim()?.takeIf { it.isNotEmpty() } }
        val missing = values.filterValues { it == null }.keys.toList()
        if (missing.isNotEmpty()) return SigningResolution.DebugFallback(missing)
        return SigningResolution.Release(
            ReleaseSigning(
                storeFile = values.getValue(KEYSTORE_PATH)!!,
                storePassword = values.getValue(KEYSTORE_PASSWORD)!!,
                keyAlias = values.getValue(KEY_ALIAS)!!,
                keyPassword = values.getValue(KEY_PASSWORD)!!,
            ),
        )
    }
}
