package studio.mialdea.floorday.core.data.repository

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import studio.mialdea.floorday.core.data.db.inMemoryDatabase
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.time.TestDates

/** Room-only details on top of RoomRepositoryContractTest. */
class RoomHabitRepositoryTest {
    private val database = inMemoryDatabase()
    private val habits = RoomHabitRepository(database)
    private val completions = RoomCompletionRepository(database)
    private val day = TestDates.SPRINT0_END

    @AfterTest
    fun close() = database.close()

    @Test
    fun `reordenar los momentos y guardar conserva el orden nuevo y el historial`() = runTest {
        val habit = aHabit { slot { label = "Mañana" }; slot { label = "Tarde" }; slot { label = "Noche" } }
        habits.save(habit)
        habit.slots.forEach { completions.upsert(aCompletion(it, day)) }

        val reordered = habit.copy(slots = habit.slots.reversed())
        habits.save(reordered)

        assertEquals(reordered.slots, habits.getHabit(habit.id)?.slots)
        assertEquals(3, completions.observeDay(day).first().size)
    }

    @Test
    fun `guardar de nuevo el mismo habito no borra sus marcas`() = runTest {
        // If @Upsert became REPLACE, the row would be deleted and the CASCADE would take the history.
        val habit = aHabit()
        habits.save(habit)
        completions.upsert(aCompletion(habit.slots.single(), day))

        habits.save(habit.copy(name = "Beber más agua"))

        assertEquals(1, completions.observeDay(day).first().size)
    }
}
