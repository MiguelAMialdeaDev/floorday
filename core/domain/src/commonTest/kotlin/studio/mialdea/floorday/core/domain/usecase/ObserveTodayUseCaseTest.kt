package studio.mialdea.floorday.core.domain.usecase

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.time.LogicalDayProvider
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.sampleMinimumDay
import studio.mialdea.floorday.core.testing.repository.InMemoryCompletionRepository
import studio.mialdea.floorday.core.testing.repository.InMemoryFloordayStore
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository

class ObserveTodayUseCaseTest {

    private val day = LocalDate(2026, 12, 1)
    private val store = InMemoryFloordayStore()
    private val habits = InMemoryHabitRepository(store)
    private val completions = InMemoryCompletionRepository(store)
    private val days = FakeLogicalDayProvider(day)
    private val observeToday = ObserveTodayUseCase(habits, completions, days)

    @Test
    fun `las filas siguen el orden del usuario y el anillo sale del evaluador`() = runTest {
        val sample = sampleMinimumDay()
        habits.seed(*sample.toTypedArray())
        val morningDose = sample[0].slots[0]
        completions.seed(aCompletion(morningDose, day))

        observeToday().test {
            val today = awaitItem()
            assertEquals(listOf("Vitamina D", "Vitamina D", "Caminar 10 minutos", "Leer 5 páginas", "Estirar"),
                today.entries.map { it.habit.name })
            assertEquals(listOf(true, true, true, true, false), today.entries.map { it.required })
            assertEquals(morningDose.id, today.entries[0].completion?.slotId)
            assertEquals(1 to 4, today.minimum.done to today.minimum.required)
            assertTrue(today.hasHabits)
        }
    }

    @Test
    fun `un dia nuevo empieza sin las marcas de ayer`() = runTest {
        val sample = sampleMinimumDay()
        habits.seed(*sample.toTypedArray())
        completions.seed(aCompletion(sample[0].slots[0], day))

        observeToday().test {
            assertEquals(1, awaitItem().minimum.done)
            days.current.value = LocalDate(2026, 12, 2)
            val tomorrow = awaitItem()
            assertEquals(0, tomorrow.minimum.done)
            assertNull(tomorrow.entries[0].completion)
        }
    }

    private class FakeLogicalDayProvider(start: LocalDate) : LogicalDayProvider {
        val current = MutableStateFlow(start)
        override fun observe(): Flow<LocalDate> = current
        override fun refresh() = Unit
    }
}
