package studio.mialdea.floorday.buildlogic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ReleaseSigningResolverTest {
    private val complete = mapOf(
        "FLOORDAY_KEYSTORE_PATH" to " /keys/upload.jks ",
        "FLOORDAY_KEYSTORE_PASSWORD" to "store",
        "FLOORDAY_KEY_ALIAS" to "upload",
        "FLOORDAY_KEY_PASSWORD" to "key",
    )

    @Test fun allKeysGiveReleaseSigningTrimmed() {
        val result = assertIs<SigningResolution.Release>(ReleaseSigningResolver.resolve(complete::get))
        assertEquals(ReleaseSigning("/keys/upload.jks", "store", "upload", "key"), result.signing)
    }

    @Test fun noKeysFallBackToDebugListingAllInOrder() {
        val result = assertIs<SigningResolution.DebugFallback>(ReleaseSigningResolver.resolve { null })
        assertEquals(ReleaseSigningResolver.KEYS, result.missingKeys)
    }

    @Test fun blankValueCountsAsMissing() {
        val props = complete + ("FLOORDAY_KEY_PASSWORD" to "   ")
        val result = assertIs<SigningResolution.DebugFallback>(ReleaseSigningResolver.resolve(props::get))
        assertEquals(listOf("FLOORDAY_KEY_PASSWORD"), result.missingKeys)
    }

    @Test fun partialConfigListsOnlyMissingKeys() {
        val props = complete - "FLOORDAY_KEY_ALIAS" - "FLOORDAY_KEYSTORE_PASSWORD"
        val result = assertIs<SigningResolution.DebugFallback>(ReleaseSigningResolver.resolve(props::get))
        assertEquals(listOf("FLOORDAY_KEYSTORE_PASSWORD", "FLOORDAY_KEY_ALIAS"), result.missingKeys)
    }
}
