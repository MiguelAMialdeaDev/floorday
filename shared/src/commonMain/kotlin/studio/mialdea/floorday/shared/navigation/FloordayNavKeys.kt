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
 * Cada NavKey nueva TIENE que registrarse aquí y en FloordayNavKeysTest. En Android funciona sin
 * registrarla, pero en iOS falla al guardar el back stack (polimorfismo abierto sin reflexión).
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
