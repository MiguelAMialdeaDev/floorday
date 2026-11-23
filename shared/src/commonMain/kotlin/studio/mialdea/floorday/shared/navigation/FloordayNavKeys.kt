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
 * Habit editor; [habitId] null = new habit. The id goes as text so the key needs no Uuid
 * serializer and restores the same on Android and iOS.
 */
@Serializable
data class HabitEditorKey(val habitId: String? = null) : NavKey

/**
 * Every new NavKey MUST be registered here and in FloordayNavKeysTest. On Android it works without
 * registering it, but on iOS it fails when saving the back stack (open polymorphism, no reflection).
 */
val floordayNavKeyModule: SerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(TodayKey::class)
        subclass(HistoryKey::class)
        subclass(SettingsKey::class)
        subclass(HabitEditorKey::class)
    }
}

val floordaySavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = floordayNavKeyModule
}
