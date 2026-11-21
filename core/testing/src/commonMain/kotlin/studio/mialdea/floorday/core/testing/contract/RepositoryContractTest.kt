package studio.mialdea.floorday.core.testing.contract

import app.cash.turbine.test
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.domain.repository.CompletionRepository
import studio.mialdea.floorday.core.domain.repository.HabitRepository
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.builder.aMedication
import studio.mialdea.floorday.core.testing.time.FakeClock
import studio.mialdea.floorday.core.testing.time.TestDates

/**
 * Behaviour every HabitRepository + CompletionRepository pair must share: the in-memory fakes
 * and the Room implementation run these same tests, so a fake cannot drift from production.
 * Subclasses create fresh, empty repositories sharing one store for each test.
 */
abstract class RepositoryContractTest {

    protected class Repositories(
        val habits: HabitRepository,
        val completions: CompletionRepository,
        val close: () -> Unit = {},
    )

    protected abstract fun createRepositories(): Repositories

    private val repositories by lazy { createRepositories() }
    private val habits get() = repositories.habits
    private val completions get() = repositories.completions
    private val day = TestDates.SPRINT0_END

    @AfterTest
    fun closeRepositories() {
        repositories.close()
    }

    @Test
    fun `guardar y leer devuelve el mismo habito con sus momentos en su orden`() = runTest {
        val habit = aMedication {
            unitLabel = "pastilla"
            archivedOn = TestDates.DST_SPRING_2027
            updatedAt = FakeClock.DEFAULT_START + 2.hours
            slot { label = "Noche"; time = LocalTime(22, 0); amount = Quarters.HALF; reminder = ReminderMode.INSISTENT }
            slot { label = "Mañana"; time = LocalTime(8, 0); amount = Quarters.ONE; reminder = ReminderMode.INSISTENT }
            slot { label = "Cuando sea"; amount = Quarters(6) }
        }

        habits.save(habit)

        assertEquals(habit, habits.getHabit(habit.id))
    }

    @Test
    fun `leer un habito inexistente devuelve null`() = runTest {
        assertNull(habits.getHabit(aHabit().id))
    }

    @Test
    fun `observeHabits ordena por sortOrder y despues por nombre`() = runTest {
        val c = aHabit { name = "C"; sortOrder = 0 }
        val b = aHabit { name = "B"; sortOrder = 1 }
        val a = aHabit { name = "A"; sortOrder = 1 }

        listOf(b, c, a).forEach { habits.save(it) }

        assertEquals(listOf(c, a, b), habits.observeHabits().first())
    }

    @Test
    fun `observeHabits emite despues de cada guardado`() = runTest {
        val habit = aHabit()

        habits.observeHabits().test {
            assertEquals(emptyList<Habit>(), awaitItem())
            habits.save(habit)
            assertEquals(listOf(habit), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `los archivados solo salen si se piden y desarchivar los devuelve`() = runTest {
        val active = aHabit { name = "Agua"; sortOrder = 0 }
        val archived = aHabit { name = "Leer"; sortOrder = 1 }
        habits.save(active)
        habits.save(archived)

        habits.archive(archived.id, on = day)

        assertEquals(listOf(active.id), habits.observeHabits().first().map { it.id })
        assertEquals(day, habits.observeHabits(includeArchived = true).first().last().archivedOn)

        habits.archive(archived.id, on = null)

        assertEquals(listOf(active.id, archived.id), habits.observeHabits().first().map { it.id })
    }

    @Test
    fun `archivar un habito inexistente no hace nada`() = runTest {
        habits.archive(aHabit().id, on = day)

        assertTrue(habits.observeHabits(includeArchived = true).first().isEmpty())
    }

    @Test
    fun `guardar sin un momento borra solo las marcas de ese momento`() = runTest {
        val habit = aHabit { slot { label = "Mañana" }; slot { label = "Noche" } }
        val (morning, night) = habit.slots
        habits.save(habit)
        completions.upsert(aCompletion(morning, day))
        completions.upsert(aCompletion(night, day))

        habits.save(habit.copy(name = "Beber más agua", slots = listOf(morning)))

        assertEquals(listOf(morning), habits.getHabit(habit.id)?.slots)
        assertEquals(listOf(morning.id), completions.observeDay(day).first().map { it.slotId })
    }

    @Test
    fun `borrar un habito borra sus momentos y sus marcas`() = runTest {
        val habit = aHabit()
        val other = aHabit { name = "Estirar" }
        habits.save(habit)
        habits.save(other)
        completions.upsert(aCompletion(habit.slots.single(), day))
        completions.upsert(aCompletion(other.slots.single(), day))

        habits.delete(habit.id)

        assertNull(habits.getHabit(habit.id))
        assertEquals(listOf(other.slots.single().id), completions.observeDay(day).first().map { it.slotId })
    }

    @Test
    fun `nextSortOrder es el maximo mas uno o cero sin habitos`() = runTest {
        assertEquals(0, habits.nextSortOrder())

        habits.save(aHabit { sortOrder = 0 })
        habits.save(aHabit { sortOrder = 4 })

        assertEquals(5, habits.nextSortOrder())
    }

    @Test
    fun `marcar otra vez el mismo momento y dia reemplaza la marca`() = runTest {
        val medication = aMedication()
        val slot = medication.slots.single()
        habits.save(medication)

        completions.upsert(aCompletion(slot, day, status = CompletionStatus.DONE))
        completions.upsert(aCompletion(slot, day, status = CompletionStatus.SKIPPED))

        assertEquals(CompletionStatus.SKIPPED, completions.observeDay(day).first().single().status)
    }

    @Test
    fun `remove deshace la marca y no falla si no existe`() = runTest {
        val slot = aHabit().also { habits.save(it) }.slots.single()
        completions.upsert(aCompletion(slot, day))

        completions.remove(slot.id, day)
        completions.remove(slot.id, day)

        assertTrue(completions.observeDay(day).first().isEmpty())
    }

    @Test
    fun `observeDay devuelve solo las marcas del dia ordenadas por hora de registro`() = runTest {
        val habit = aHabit { slot { label = "Mañana" }; slot { label = "Noche" } }
        val (morning, night) = habit.slots
        habits.save(habit)
        val late = aCompletion(morning, day, recordedAt = FakeClock.DEFAULT_START + 3.hours)
        val early = aCompletion(night, day, recordedAt = FakeClock.DEFAULT_START)
        val otherDay = aCompletion(morning, TestDates.BASELINE)

        listOf(late, otherDay, early).forEach { completions.upsert(it) }

        assertEquals(listOf(early, late), completions.observeDay(day).first())
    }

    @Test
    fun `una marca de un momento inexistente falla y no se guarda`() = runTest {
        val orphan = aHabit().slots.single()

        assertFails { completions.upsert(aCompletion(orphan, day)) }

        assertTrue(completions.observeDay(day).first().isEmpty())
    }
}
