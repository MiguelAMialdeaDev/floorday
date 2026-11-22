package studio.mialdea.floorday.core.domain.usecase

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.EntrySource
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aMedication
import studio.mialdea.floorday.core.testing.repository.InMemoryCompletionRepository
import studio.mialdea.floorday.core.testing.repository.InMemoryFloordayStore
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository
import studio.mialdea.floorday.core.testing.time.FakeClock

class ToggleSlotUseCaseTest {

    private val day = LocalDate(2026, 12, 1)
    private val clock = FakeClock()
    private val store = InMemoryFloordayStore()
    private val habits = InMemoryHabitRepository(store)
    private val completions = InMemoryCompletionRepository(store)
    private val toggle = ToggleSlotUseCase(completions, clock)
    private val restore = RestoreCompletionUseCase(completions)
    private val habit = aMedication { slot { amount = Quarters.HALF } }
    private val slot = habit.slots[0]

    @Test
    fun `un momento sin marcar pasa a hecho con su cantidad y la hora`() = runTest {
        habits.seed(habit)

        val outcome = toggle(habit, slot.id, day, previous = null)

        assertNull(outcome.previous)
        val done = requireNotNull(outcome.current)
        assertEquals(CompletionStatus.DONE, done.status)
        assertEquals(Quarters.HALF, done.amount)
        assertEquals(clock.now(), done.recordedAt)
        assertEquals(EntrySource.APP, done.source)
        completions.observeDay(day).test { assertEquals(listOf(done), awaitItem()) }
    }

    @Test
    fun `un momento hecho se desmarca y deshacer devuelve la misma marca`() = runTest {
        val earlier = aCompletion(slot, day)
        habits.seed(habit)
        completions.seed(earlier)

        val outcome = toggle(habit, slot.id, day, previous = earlier)
        assertNull(outcome.current)
        completions.observeDay(day).test { assertEquals(emptyList(), awaitItem()) }

        restore(slot.id, day, outcome.previous)
        completions.observeDay(day).test { assertEquals(listOf(earlier), awaitItem()) }
    }

    @Test
    fun `deshacer una primera marca deja el momento sin marcar`() = runTest {
        habits.seed(habit)
        val outcome = toggle(habit, slot.id, day, previous = null)

        restore(slot.id, day, outcome.previous)

        completions.observeDay(day).test { assertEquals(emptyList(), awaitItem()) }
    }
}
