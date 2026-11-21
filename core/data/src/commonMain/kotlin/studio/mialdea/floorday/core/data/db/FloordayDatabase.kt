package studio.mialdea.floorday.core.data.db

import androidx.room3.BuiltInColumnTypeConverters
import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import studio.mialdea.floorday.core.data.db.dao.CompletionDao
import studio.mialdea.floorday.core.data.db.dao.HabitDao
import studio.mialdea.floorday.core.data.db.entity.CompletionEntity
import studio.mialdea.floorday.core.data.db.entity.HabitEntity
import studio.mialdea.floorday.core.data.db.entity.HabitSlotEntity

/** Raising it requires the exported JSON committed, a migration and a test in FloordayDatabaseMigrationTest. */
const val FLOORDAY_DB_VERSION = 1

@Database(
    entities = [HabitEntity::class, HabitSlotEntity::class, CompletionEntity::class],
    version = FLOORDAY_DB_VERSION,
    exportSchema = true,
)
@ColumnTypeConverters(
    FloordayConverters::class,
    // Built-in converters off: Uuid would become BLOB and enums their name(). Ours store TEXT keys,
    // and a column without a converter fails at compile time instead of changing silently.
    builtInColumnTypeConverters = BuiltInColumnTypeConverters(
        enums = BuiltInColumnTypeConverters.State.DISABLED,
        uuid = BuiltInColumnTypeConverters.State.DISABLED,
    ),
)
@ConstructedBy(FloordayDatabaseConstructor::class)
abstract class FloordayDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    abstract fun completionDao(): CompletionDao
}

// Room generates the `actual` of every target (floorday.room registers the compiler per target).
@Suppress("KotlinNoActualForExpect")
expect object FloordayDatabaseConstructor : RoomDatabaseConstructor<FloordayDatabase> {
    override fun initialize(): FloordayDatabase
}
