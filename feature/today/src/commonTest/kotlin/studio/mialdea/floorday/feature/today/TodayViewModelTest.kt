@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.mialdea.floorday.feature.today

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.designsystem.component.CheckState
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.SlotId
import studio.mialdea.floorday.core.domain.repository.CompletionRepository
import studio.mialdea.floorday.core.domain.usecase.ObserveTodayUseCase
import studio.mialdea.floorday.core.domain.usecase.RestoreCompletionUseCase
import studio.mialdea.floorday.core.domain.usecase.ToggleSlotUseCase
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.sampleMinimumDay
import studio.mialdea.floorday.core.testing.coroutines.runMainTest
import studio.mialdea.floorday.core.testing.repository.InMemoryCompletionRepository
import studio.mialdea.floorday.core.testing.repository.InMemoryFloordayStore
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository
import studio.mialdea.floorday.core.testing.time.FakeClock
import studio.mialdea.floorday.core.testing.time.FakeLogicalDayProvider
import studio.mialdea.floorday.core.testing.time.FakeTimeZoneProvider

class TodayViewModelTest {

    private val day = LocalDate(2026, 12, 1)
    private val sample = sampleMinimumDay()
    private val walk = sample[1]
    private val walkSlot = walk.slots[0].id
    private val store = InMemoryFloordayStore()
    private val habits = InMemoryHabitRepository(store).apply { seed(*sample.toTypedArray()) }
    private val completions = FlakyCompletionRepository(InMemoryCompletionRepository(store))
    private val days = FakeLogicalDayProvider(day)

    private fun TestScope.loadedViewModel(): TodayViewModel {
        val viewModel = TodayViewModel(
            observeToday = ObserveTodayUseCase(habits, completions, days),
            toggleSlot = ToggleSlotUseCase(completions, FakeClock()),
            restoreCompletion = RestoreCompletionUseCase(completions),
            logicalDay = days,
            timeZones = FakeTimeZoneProvider(),
            timeFormatter = { it.toString() },
        )
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `al cargar muestra el dia con el anillo a cero`() = runMainTest {
        val viewModel = loadedViewModel()

        val content = viewModel.state.value.content!!
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(day, content.day)
        assertEquals(0 to 4, content.done to content.required)
    }

    @Test
    fun `un toque marca la fila guarda la marca y ofrece deshacer`() = runMainTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
            advanceUntilIdle()

            val undo = awaitItem() as TodayEffect.ShowUndo
            assertEquals(walk.name to true, undo.habitName to undo.markedDone)
        }
        assertEquals(CheckState.Done, viewModel.rowState(walkSlot))
        assertEquals(CompletionStatus.DONE, marks().single().status)
    }

    @Test
    fun `deshacer una marca nueva deja la fila pendiente y sin marca`() = runMainTest {
        val viewModel = loadedViewModel()
        viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
        advanceUntilIdle()

        viewModel.onIntent(TodayIntent.Undo(viewModel.state.value.pendingUndo!!.id))
        advanceUntilIdle()

        assertEquals(CheckState.Pending, viewModel.rowState(walkSlot))
        assertEquals(emptyList(), marks())
        assertNull(viewModel.state.value.pendingUndo)
    }

    @Test
    fun `desmarcar y deshacer devuelve la marca original con su hora`() = runMainTest {
        val original = aCompletion(walk.slots[0], day)
        completions.delegate.seed(original)
        val viewModel = loadedViewModel()

        viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
        advanceUntilIdle()
        assertEquals(emptyList(), marks())

        viewModel.onIntent(TodayIntent.Undo(viewModel.state.value.pendingUndo!!.id))
        advanceUntilIdle()

        assertEquals(listOf(original), marks())
        assertEquals(CheckState.Done, viewModel.rowState(walkSlot))
    }

    @Test
    fun `si falla la escritura la fila vuelve atras y avisa`() = runMainTest {
        val viewModel = loadedViewModel()
        completions.failWrites = true

        viewModel.effects.test {
            viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
            advanceUntilIdle()

            assertEquals(TodayEffect.ShowSaveError, awaitItem())
        }
        assertEquals(CheckState.Pending, viewModel.rowState(walkSlot))
        assertNull(viewModel.state.value.pendingUndo)
    }

    @Test
    fun `un dia nuevo empieza sin marcas y sin el deshacer de ayer`() = runMainTest {
        val viewModel = loadedViewModel()
        viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
        advanceUntilIdle()

        days.current.value = LocalDate(2026, 12, 2)
        advanceUntilIdle()

        val content = viewModel.state.value.content!!
        assertEquals(LocalDate(2026, 12, 2), content.day)
        assertEquals(0, content.done)
        assertNull(viewModel.state.value.pendingUndo)
    }

    @Test
    fun `volver a la app pide recalcular el dia`() = runMainTest {
        val viewModel = loadedViewModel()

        viewModel.onIntent(TodayIntent.Resumed)
        advanceUntilIdle()

        assertEquals(1, days.refreshes)
    }

    @Test
    fun `la fila y el anillo cambian antes de que termine la escritura`() = runMainTest {
        val viewModel = loadedViewModel()
        completions.holdWrites = true

        viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
        runCurrent()

        assertEquals(CheckState.Done, viewModel.rowState(walkSlot))
        assertEquals(1, viewModel.state.value.content!!.done)
        assertEquals(emptyList(), marks())
    }

    @Test
    fun `dos toques rapidos no hacen parpadear la fila`() = runMainTest {
        val viewModel = loadedViewModel()
        completions.holdWrites = true
        viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
        viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
        runCurrent()

        // The first write lands and Room emits Done while the second tap is already waiting.
        completions.releaseOneWrite()
        advanceUntilIdle()

        assertEquals(CheckState.Pending, viewModel.rowState(walkSlot))
        completions.releaseOneWrite()
        advanceUntilIdle()
        assertEquals(emptyList(), marks())
        assertEquals(CheckState.Pending, viewModel.rowState(walkSlot))
    }

    @Test
    fun `si el dia cambia durante la escritura el dia nuevo queda limpio`() = runMainTest {
        val viewModel = loadedViewModel()
        completions.holdWrites = true
        viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
        runCurrent()
        val tomorrow = LocalDate(2026, 12, 2)

        days.current.value = tomorrow
        advanceUntilIdle()
        completions.releaseOneWrite()
        advanceUntilIdle()

        assertEquals(CheckState.Pending, viewModel.rowState(walkSlot))
        assertNull(viewModel.state.value.pendingUndo)
        completions.holdWrites = false
        viewModel.onIntent(TodayIntent.ToggleSlot(walkSlot))
        advanceUntilIdle()
        assertEquals(CompletionStatus.DONE, completions.observeDay(tomorrow).first().single().status)
        assertEquals(CheckState.Done, viewModel.rowState(walkSlot))
    }

    private fun TodayViewModel.rowState(slotId: SlotId): CheckState? = state.value.content?.findRow(slotId)?.state

    private suspend fun marks(): List<Completion> = completions.observeDay(day).first()

    /** In-memory repository whose writes can be made to fail or to wait until the test lets them through. */
    private class FlakyCompletionRepository(val delegate: InMemoryCompletionRepository) : CompletionRepository {
        var failWrites = false
        var holdWrites = false
        private val permits = Channel<Unit>(Channel.UNLIMITED)

        fun releaseOneWrite() {
            permits.trySend(Unit)
        }

        override fun observeDay(day: LocalDate): Flow<List<Completion>> = delegate.observeDay(day)

        override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Completion>> =
            delegate.observeRange(from, to)

        override suspend fun upsert(completion: Completion) {
            gate()
            delegate.upsert(completion)
        }

        override suspend fun remove(slotId: SlotId, day: LocalDate) {
            gate()
            delegate.remove(slotId, day)
        }

        private suspend fun gate() {
            if (holdWrites) permits.receive()
            check(!failWrites) { "disk full" }
        }
    }
}
