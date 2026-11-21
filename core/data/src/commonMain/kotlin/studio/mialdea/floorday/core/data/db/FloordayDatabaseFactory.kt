package studio.mialdea.floorday.core.data.db

import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Manual migrations (suspend migrate(connection)). New tables go as @AutoMigration in
 * FloordayDatabase and need no entry here.
 */
object FloordayMigrations {
    val ALL: List<Migration> = emptyList()
}

object FloordayDatabaseFactory {
    /** Configuration shared by every platform. No fallbackToDestructiveMigration: data is never dropped. */
    fun create(
        builder: RoomDatabase.Builder<FloordayDatabase>,
        ioDispatcher: CoroutineDispatcher,
    ): FloordayDatabase = builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(ioDispatcher)
        .addMigrations(*FloordayMigrations.ALL.toTypedArray())
        .build()
}
