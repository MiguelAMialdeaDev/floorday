package studio.mialdea.floorday.core.data.platform

import android.content.Context
import androidx.datastore.core.FileStorage
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import studio.mialdea.floorday.core.data.db.FloordayDatabase
import studio.mialdea.floorday.core.data.db.FloordayDatabaseConstructor

class AndroidDataPlatform(context: Context) : DataPlatform {
    private val appContext = context.applicationContext

    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun databaseBuilder(): RoomDatabase.Builder<FloordayDatabase> =
        Room.databaseBuilder<FloordayDatabase>(
            context = appContext,
            name = appContext.getDatabasePath(DataFiles.DATABASE).absolutePath,
            factory = { FloordayDatabaseConstructor.initialize() },
        )

    /** filesDir: included in Auto Backup (rules in ADR-0014). */
    override fun userSettingsStorage(): Storage<Preferences> = FileStorage(
        serializer = PreferencesFileSerializer,
        produceFile = { appContext.filesDir.resolve("${DataFiles.DATASTORE_DIR}/${DataFiles.USER_SETTINGS}") },
    )

    /** noBackupFilesDir: Auto Backup always excludes it. */
    override fun deviceStateStorage(): Storage<Preferences> = FileStorage(
        serializer = PreferencesFileSerializer,
        produceFile = {
            appContext.noBackupFilesDir.resolve("${DataFiles.DATASTORE_DIR}/${DataFiles.DEVICE_STATE}")
        },
    )
}
