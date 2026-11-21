package studio.mialdea.floorday.core.data.platform

import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences
import androidx.room3.RoomDatabase
import kotlinx.coroutines.CoroutineDispatcher
import studio.mialdea.floorday.core.data.db.FloordayDatabase

/**
 * The only things that change per platform: file locations, the Room builder (Android needs a
 * Context, so there is no common builder) and the DataStore storage. An interface instead of
 * expect/actual: Android has AndroidDataPlatform; iOS gets one in the App Group (ADR-0010).
 */
interface DataPlatform {
    val ioDispatcher: CoroutineDispatcher

    fun databaseBuilder(): RoomDatabase.Builder<FloordayDatabase>

    /** Travels in the backup. */
    fun userSettingsStorage(): Storage<Preferences>

    /** NEVER travels in the backup. */
    fun deviceStateStorage(): Storage<Preferences>
}

object DataFiles {
    const val DATABASE = "floorday.db"
    const val DATASTORE_DIR = "datastore"
    const val USER_SETTINGS = "user_settings.preferences_pb"
    const val DEVICE_STATE = "device_state.preferences_pb"
}
