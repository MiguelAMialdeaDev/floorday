package studio.mialdea.floorday.core.data.db

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Prepared migration test. With only v1 it checks that every version has its exported JSON and
 * that a database created from that JSON validates against the current entities.
 * When v2 arrives: add a test that creates v1, inserts rows, migrates and checks the data.
 */
class FloordayDatabaseMigrationTest {
    private val schemaDir: Path = Path.of(
        requireNotNull(System.getProperty("floorday.schemaDir")) {
            "Missing -Dfloorday.schemaDir (set by the floorday.room convention plugin)"
        },
    )
    private val databasePath: Path = Files.createTempDirectory("floorday-migration").resolve("migration-test.db")

    @get:Rule
    val helper = MigrationTestHelper(
        schemaDirectoryPath = schemaDir,
        databasePath = databasePath,
        driver = BundledSQLiteDriver(),
        databaseClass = FloordayDatabase::class,
        databaseFactory = { FloordayDatabaseConstructor.initialize() },
    )

    @Test
    fun `cada version tiene su esquema exportado`() {
        val databaseDir = schemaDir.resolve(requireNotNull(FloordayDatabase::class.qualifiedName))

        (1..FLOORDAY_DB_VERSION).forEach { version ->
            assertTrue(
                Files.exists(databaseDir.resolve("$version.json")),
                "Missing schemas/.../$version.json: build :core:data and commit the schema",
            )
        }
    }

    @Test
    fun `una base de datos v1 creada desde el JSON valida contra el codigo actual`() = runTest {
        helper.createDatabase(1).apply {
            execSQL(
                "INSERT INTO habit (id, name, kind, days, minimum_since, archived_on, sort_order, " +
                    "unit_label, created_at, updated_at) VALUES ('0b4f1f9e-6c1a-4c2e-9a51-1f0e2d3c4b5a', " +
                    "'Vitamina D', 'medication', 127, '2026-12-06', NULL, 0, 'pastilla', 0, 0)",
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(FLOORDAY_DB_VERSION, FloordayMigrations.ALL)

        migrated.prepare("SELECT name, kind, minimum_since FROM habit").use { statement ->
            assertTrue(statement.step())
            assertEquals("Vitamina D", statement.getText(0))
            assertEquals("medication", statement.getText(1))
            assertEquals("2026-12-06", statement.getText(2))
        }
        migrated.close()
    }
}
