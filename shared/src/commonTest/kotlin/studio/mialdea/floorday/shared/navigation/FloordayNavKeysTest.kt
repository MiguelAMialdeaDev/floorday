package studio.mialdea.floorday.shared.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class FloordayNavKeysTest {

    private val json = Json { serializersModule = floordayNavKeyModule }
    private val serializer = PolymorphicSerializer(NavKey::class)

    @Test
    fun everyTopLevelKeySurvivesPolymorphicRoundTrip() {
        val keys: List<NavKey> = listOf(TodayKey, HistoryKey, SettingsKey)

        keys.forEach { key ->
            val encoded = json.encodeToString(serializer, key)
            assertEquals(key, json.decodeFromString(serializer, encoded))
        }
    }
}
