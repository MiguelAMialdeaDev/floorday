@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.mialdea.floorday.feature.today.editor

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.repository.HabitRepository
import studio.mialdea.floorday.core.domain.usecase.DeleteHabitUseCase
import studio.mialdea.floorday.core.domain.usecase.SaveHabitUseCase
import studio.mialdea.floorday.core.testing.builder.habitIdOf
import studio.mialdea.floorday.core.testing.coroutines.runMainTest
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository
import studio.mialdea.floorday.core.testing.time.FakeClock
import studio.mialdea.floorday.core.testing.time.FakeLogicalDayProvider
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent as I

class HabitEditorViewModelTest {

    private val stored = sampleHabit()
    private val habits = FlakyHabitRepository(InMemoryHabitRepository().apply { seed(stored) })

    private fun TestScope.viewModel(habitId: HabitId?): HabitEditorViewModel {
        val viewModel = HabitEditorViewModel(
            habitId = habitId,
            habits = habits,
            saveHabit = SaveHabitUseCase(habits, FakeClock(), FakeLogicalDayProvider(LocalDate(2026, 12, 1)), IdGenerator.Random),
            deleteHabit = DeleteHabitUseCase(habits),
        )
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `guardar un habito nuevo sin nombre muestra el error y lleva el foco al nombre`() = runMainTest {
        val viewModel = viewModel(habitId = null)

        viewModel.effects.test {
            viewModel.onIntent(I.SaveClicked)
            advanceUntilIdle()

            assertEquals(HabitEditorEffect.FocusError(EditorField.NAME), awaitItem())
        }
        assertEquals(listOf(EditorError.NameBlank), viewModel.state.value.visibleErrors)
        assertEquals(0, habits.saves)
    }

    @Test
    fun `un habito nuevo valido se guarda y cierra el editor`() = runMainTest {
        val viewModel = viewModel(habitId = null)

        viewModel.effects.test {
            viewModel.onIntent(I.NameChanged("Pasear al perro"))
            viewModel.onIntent(I.SaveClicked)
            advanceUntilIdle()

            assertEquals(HabitEditorEffect.Close(CloseResult.SAVED), awaitItem())
        }
        assertTrue(habits.all().any { it.name == "Pasear al perro" })
    }

    @Test
    fun `editar carga el habito y guarda el cambio con el mismo id`() = runMainTest {
        val viewModel = viewModel(stored.id)
        assertEquals(stored.name, viewModel.state.value.name)

        viewModel.onIntent(I.NameChanged("Vitamina D3"))
        viewModel.onIntent(I.SaveClicked)
        advanceUntilIdle()

        assertEquals("Vitamina D3", habits.getHabit(stored.id)?.name)
    }

    @Test
    fun `un habito que ya no existe cierra el editor`() = runMainTest {
        val viewModel = HabitEditorViewModel(
            habitId = habitIdOf(99),
            habits = habits,
            saveHabit = SaveHabitUseCase(habits, FakeClock(), FakeLogicalDayProvider(LocalDate(2026, 12, 1)), IdGenerator.Random),
            deleteHabit = DeleteHabitUseCase(habits),
        )

        viewModel.effects.test {
            advanceUntilIdle()
            assertEquals(HabitEditorEffect.Close(CloseResult.NOT_FOUND), awaitItem())
        }
    }

    @Test
    fun `si falla la escritura vuelve a editar con aviso y sin perder el borrador`() = runMainTest {
        val viewModel = viewModel(stored.id)
        habits.failWrites = true

        viewModel.onIntent(I.NameChanged("Vitamina D3"))
        viewModel.onIntent(I.SaveClicked)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(HabitEditorState.Phase.EDITING, state.phase)
        assertTrue(state.writeFailed)
        assertEquals("Vitamina D3", state.name)

        habits.failWrites = false
        viewModel.onIntent(I.SaveClicked)
        advanceUntilIdle()
        assertEquals("Vitamina D3", habits.getHabit(stored.id)?.name)
    }

    @Test
    fun `dos toques seguidos en guardar escriben una sola vez`() = runMainTest {
        val viewModel = viewModel(stored.id)

        viewModel.onIntent(I.SaveClicked)
        viewModel.onIntent(I.SaveClicked)
        advanceUntilIdle()

        assertEquals(1, habits.saves)
    }

    @Test
    fun `borrar pide confirmacion y luego borra y cierra`() = runMainTest {
        val viewModel = viewModel(stored.id)

        viewModel.effects.test {
            viewModel.onIntent(I.DeleteConfirmed)
            advanceUntilIdle()
            assertEquals(stored, habits.getHabit(stored.id), "sin confirmar no borra")

            viewModel.onIntent(I.DeleteClicked)
            viewModel.onIntent(I.DeleteConfirmed)
            advanceUntilIdle()

            assertEquals(HabitEditorEffect.Close(CloseResult.DELETED), awaitItem())
        }
        assertNull(habits.getHabit(stored.id))
    }

    @Test
    fun `cancelar cierra sin guardar`() = runMainTest {
        val viewModel = viewModel(stored.id)

        viewModel.effects.test {
            viewModel.onIntent(I.NameChanged("Otro"))
            viewModel.onIntent(I.CancelClicked)
            advanceUntilIdle()

            assertEquals(HabitEditorEffect.Close(CloseResult.CANCELLED), awaitItem())
        }
        assertEquals(stored, habits.getHabit(stored.id))
    }
}

/** Counts saves and can make every write fail, like a full disk. */
private class FlakyHabitRepository(private val delegate: InMemoryHabitRepository) : HabitRepository by delegate {
    var failWrites = false
    var saves = 0
        private set

    override suspend fun save(habit: Habit) {
        check(!failWrites) { "disk full" }
        saves++
        delegate.save(habit)
    }

    override suspend fun delete(id: HabitId) {
        check(!failWrites) { "disk full" }
        delegate.delete(id)
    }

    suspend fun all(): List<Habit> = delegate.observeHabits().first()
}
