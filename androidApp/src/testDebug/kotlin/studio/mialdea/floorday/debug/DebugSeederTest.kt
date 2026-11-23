package studio.mialdea.floorday.debug

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.time.LogicalDayResolver
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository
import studio.mialdea.floorday.core.testing.time.FakeClock
import studio.mialdea.floorday.core.testing.time.FakeTimeZoneProvider

class DebugSeederTest {

    /** FakeClock starts on 1-Dec-2026 at 08:00 in Madrid. */
    private val today = LocalDate(2026, 12, 1)
    private val clock = FakeClock()
    private val habits = InMemoryHabitRepository()
    private val example = File("src/debug/seed.example.json").readText()

    private fun seeder(seed: String?) = DebugSeeder(habits, clock, LogicalDayResolver(clock, FakeTimeZoneProvider())) { seed }

    @Test
    fun `el ejemplo del repo se lee con sus valores y los que faltan por defecto`() {
        val parsed = SeedParser.parse(example, IdGenerator.Random, today, clock.now())

        val medication = parsed[0]
        assertEquals(HabitKind.MEDICATION, medication.kind)
        assertEquals("pastilla", medication.unitLabel)
        assertEquals(listOf(LocalTime(8, 0), LocalTime(22, 0)), medication.slots.map { it.time })
        assertEquals(listOf(Quarters.ONE, Quarters.HALF), medication.slots.map { it.amount })
        val stretch = parsed[3]
        assertEquals(HabitKind.CHECK to 1, stretch.kind to stretch.slots.size)
        assertEquals(DaysOfWeek.EVERY_DAY, stretch.days)
        assertNull(stretch.minimumSince)
        assertEquals(listOf(today, today, today, null), parsed.map { it.minimumSince })
        assertEquals(listOf(0, 1, 2, 3), parsed.map { it.sortOrder })
    }

    @Test
    fun `un tipo desconocido falla en vez de guardar medio habito`() {
        val seed = """{ "habits": [ { "name": "Agua", "kind": "count" } ] }"""

        assertFailsWith<IllegalArgumentException> { SeedParser.parse(seed, IdGenerator.Random, today, clock.now()) }
    }

    @Test
    fun `con la base vacia guarda los habitos de la semilla`() = runTest {
        val saved = seeder(example).seedIfEmpty()

        assertEquals(4, saved)
        assertEquals(
            listOf("Vitamina D", "Beber agua", "Caminar 10 minutos", "Estirar"),
            habits.observeHabits().first().map { it.name },
        )
    }

    @Test
    fun `si ya hay habitos no toca nada`() = runTest {
        habits.seed(aHabit { name = "Leer" })

        val saved = seeder(example).seedIfEmpty()

        assertEquals(0, saved)
        assertEquals(listOf("Leer"), habits.observeHabits().first().map { it.name })
    }

    @Test
    fun `sin fichero de semilla no hace nada`() = runTest {
        assertEquals(0, seeder(null).seedIfEmpty())
    }
}
