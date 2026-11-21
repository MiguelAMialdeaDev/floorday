package studio.mialdea.floorday.core.data.db

import androidx.room3.Room
import kotlinx.coroutines.Dispatchers

/** Same configuration as production (bundled driver, migrations), but in memory. */
fun inMemoryDatabase(): FloordayDatabase = FloordayDatabaseFactory.create(
    Room.inMemoryDatabaseBuilder<FloordayDatabase>(factory = { FloordayDatabaseConstructor.initialize() }),
    Dispatchers.IO,
)
