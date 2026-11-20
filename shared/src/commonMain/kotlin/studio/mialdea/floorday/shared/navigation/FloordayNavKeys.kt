package studio.mialdea.floorday.shared.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data object TodayKey : NavKey

@Serializable
data object HistoryKey : NavKey

@Serializable
data object SettingsKey : NavKey

/**
 * Every new NavKey MUST be registered here and in FloordayNavKeysTest. On Android it works without
 * registering it, but on iOS it fails when saving the back stack (open polymorphism, no reflection).
 */
val floordayNavKeyModule: SerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(TodayKey::class)
        subclass(HistoryKey::class)
        subclass(SettingsKey::class)
    }
}

val floordaySavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = floordayNavKeyModule
}
