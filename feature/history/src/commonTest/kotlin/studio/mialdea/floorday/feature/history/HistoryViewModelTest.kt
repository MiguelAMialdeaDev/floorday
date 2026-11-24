@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.mialdea.floorday.feature.history

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.history.HabitDayMark.DONE
import studio.mialdea.floorday.core.domain.history.HabitDayMark.OPEN
import studio.mialdea.floorday.core.domain.usecase.ObserveHabitWeekUseCase
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.coroutines.runMainTest
import studio.mialdea.floorday.core.testing.repository.InMemoryCompletionRepository
import studio.mialdea.floorday.core.testing.repository.InMemoryFloordayStore
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository
import studio.mialdea.floorday.core.testing.time.FakeLogicalDayProvider
import studio.mialdea.floorday.core.testing.time.FakeTimeZoneProvider

class HistoryViewModelTest {

    private val today = LocalDate(2026, 12, 7)
    private val store = InMemoryFloordayStore()
    private val habits = InMemoryHabitRepository(store)
    private val completions = InMemoryCompletionRepository(store)
    private val days = FakeLogicalDayProvider(today)

    private fun TestScope.loadedViewModel(): HistoryViewModel {
        val viewModel = HistoryViewModel(
            observeWeek = ObserveHabitWeekUseCase(habits, completions, days, FakeTimeZoneProvider()),
            logicalDay = days,
        )
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `al cargar muestra siete dias que acaban hoy`() = runMainTest {
        habits.seed(aHabit { name = "Leer" })

        val state = loadedViewModel().state.value

        assertEquals(7, state.days.size)
        assertEquals(today, state.days.last())
        assertEquals(listOf("Leer"), state.rows.map { it.name })
    }

    @Test
    fun `marcar hoy en Hoy cambia el ultimo cuadrado a hecho`() = runMainTest {
        val habit = aHabit()
        habits.seed(habit)
        val viewModel = loadedViewModel()
        assertEquals(OPEN, viewModel.state.value.rows.single().marks.last())

        completions.upsert(aCompletion(habit.slots.single(), today))
        advanceUntilIdle()

        assertEquals(DONE, viewModel.state.value.rows.single().marks.last())
    }

    @Test
    fun `sin habitos muestra el estado vacio`() = runMainTest {
        assertTrue(loadedViewModel().state.value.isEmpty)
    }

    @Test
    fun `al volver a la app pide recalcular el dia logico`() = runMainTest {
        val viewModel = loadedViewModel()

        viewModel.onIntent(HistoryIntent.Resumed)
        advanceUntilIdle()

        assertEquals(1, days.refreshes)
    }
}
