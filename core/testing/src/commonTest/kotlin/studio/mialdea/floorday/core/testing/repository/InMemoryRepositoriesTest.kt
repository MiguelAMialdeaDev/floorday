package studio.mialdea.floorday.core.testing.repository

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.time.TestDates

class InMemoryRepositoriesTest {

    private val store = InMemoryFloordayStore()
    private val habits = InMemoryHabitRepository(store)
    private val completions = InMemoryCompletionRepository(store)
    private val day = TestDates.SPRINT0_END

    @Test
    fun `observeHabits emite ordenado por sortOrder`() = runTest {
        val b = aHabit { name = "B"; sortOrder = 1 }
        val a = aHabit { name = "A"; sortOrder = 0 }

        habits.observeHabits().test {
            assertEquals(emptyList<Habit>(), awaitItem())
            habits.save(b)
            assertEquals(listOf(b), awaitItem())
            habits.save(a)
            assertEquals(listOf(a, b), awaitItem())
        }
    }

    @Test
    fun `los archivados solo salen si se piden`() = runTest {
        val habit = aHabit()
        habits.seed(habit)

        habits.archive(habit.id, on = day)

        assertTrue(habits.observeHabits().first().isEmpty())
        assertEquals(day, habits.observeHabits(includeArchived = true).first().single().archivedOn)
    }

    @Test
    fun `borrar un habito borra sus marcas en cascada`() = runTest {
        val habit = aHabit()
        habits.seed(habit)
        completions.seed(aCompletion(habit.slots.single(), day))

        habits.delete(habit.id)

        assertTrue(completions.observeDay(day).first().isEmpty())
    }

    @Test
    fun `guardar sin un momento con marcas falla como la FK de Room`() = runTest {
        val habit = aHabit { slot { label = "Mañana" }; slot { label = "Noche" } }
        val (morning, night) = habit.slots
        habits.seed(habit)
        completions.seed(aCompletion(morning, day), aCompletion(night, day))

        val error = assertFailsWith<IllegalStateException> { habits.save(habit.copy(slots = listOf(morning))) }

        assertTrue("removedOn" in error.message.orEmpty())
        assertEquals(setOf(morning.id, night.id), completions.observeDay(day).first().map { it.slotId }.toSet())
    }

    @Test
    fun `remove deshace la marca del dia`() = runTest {
        val habit = aHabit()
        habits.seed(habit)
        completions.upsert(aCompletion(habit.slots.single(), day))

        completions.remove(habit.slots.single().id, day)

        assertTrue(completions.observeDay(day).first().isEmpty())
    }

    @Test
    fun `una marca de un momento inexistente falla`() = runTest {
        val orphan = aHabit().slots.single()

        assertFailsWith<IllegalStateException> { completions.upsert(aCompletion(orphan, day)) }
    }

    @Test
    fun `nextSortOrder es el maximo mas uno`() = runTest {
        assertEquals(0, habits.nextSortOrder())

        habits.seed(aHabit { sortOrder = 0 }, aHabit { sortOrder = 4 })

        assertEquals(5, habits.nextSortOrder())
    }

    @Test
    fun `failNextWrite falla una sola escritura`() = runTest {
        store.failNextWrite = true

        assertFailsWith<IllegalStateException> { habits.save(aHabit()) }
        habits.save(aHabit())

        assertEquals(1, habits.observeHabits().first().size)
    }

    @Test
    fun `failWith hace fallar las escrituras pero no la siembra`() = runTest {
        store.failWith = IllegalStateException("disco lleno")

        assertFailsWith<IllegalStateException> { habits.save(aHabit()) }
        habits.seed(aHabit())

        assertEquals(1, habits.observeHabits().first().size)
    }

    @Test
    fun `los ajustes falsos se actualizan con una transformacion`() = runTest {
        val settings = FakeUserSettingsRepository()

        settings.update { it.copy(onboardingDone = true) }

        assertTrue(settings.settings.first().onboardingDone)
    }
}
