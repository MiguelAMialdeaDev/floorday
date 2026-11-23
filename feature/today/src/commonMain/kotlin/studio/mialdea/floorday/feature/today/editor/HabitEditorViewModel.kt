package studio.mialdea.floorday.feature.today.editor

import androidx.lifecycle.viewModelScope
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch
import studio.mialdea.floorday.core.designsystem.mvi.MviViewModel
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.SaveHabitResult
import studio.mialdea.floorday.core.domain.repository.HabitRepository
import studio.mialdea.floorday.core.domain.usecase.DeleteHabitUseCase
import studio.mialdea.floorday.core.domain.usecase.SaveHabitUseCase

/** Habit editor. [habitId] null = create a new habit. */
class HabitEditorViewModel(
    private val habitId: HabitId?,
    private val habits: HabitRepository,
    private val saveHabit: SaveHabitUseCase,
    private val deleteHabit: DeleteHabitUseCase,
) : MviViewModel<HabitEditorState, HabitEditorIntent, HabitEditorEffect>(HabitEditorState()) {

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        if (habitId == null) {
            reduce { HabitEditorMapper.newHabit() }
            return
        }
        val habit = habits.getHabit(habitId)
        if (habit == null) {
            emit(HabitEditorEffect.Close(CloseResult.NOT_FOUND))
        } else {
            reduce { HabitEditorMapper.fromHabit(habit) }
        }
    }

    override suspend fun handle(intent: HabitEditorIntent) {
        when (intent) {
            HabitEditorIntent.SaveClicked -> save()
            HabitEditorIntent.DeleteConfirmed -> delete()
            HabitEditorIntent.CancelClicked -> emit(HabitEditorEffect.Close(CloseResult.CANCELLED))
            else -> reduce { HabitEditorReducer.reduce(this, intent) }
        }
    }

    private suspend fun save() {
        val state = currentState
        if (state.phase != HabitEditorState.Phase.EDITING) return
        val errors = HabitEditorValidator.validate(state)
        if (errors.isNotEmpty()) {
            reduce { copy(submitted = true, errors = errors, writeFailed = false) }
            emit(HabitEditorEffect.FocusError(errors.first().field))
            return
        }
        // SAVING before the first suspension: the intent queue already runs one at a time, and
        // this also makes the Save button show it is busy.
        reduce { copy(phase = HabitEditorState.Phase.SAVING, writeFailed = false) }
        val result = write { saveHabit(HabitEditorMapper.toInput(state)) } ?: return
        // S3: ReminderSync reschedules the habit's alarms here.
        val closeResult = when (result) {
            is SaveHabitResult.Saved -> CloseResult.SAVED
            SaveHabitResult.NotFound -> CloseResult.NOT_FOUND
        }
        emit(HabitEditorEffect.Close(closeResult))
    }

    private suspend fun delete() {
        val state = currentState
        val id = state.habitId
        if (!state.confirmingDelete || !state.canDelete || id == null) return
        reduce { copy(phase = HabitEditorState.Phase.SAVING, confirmingDelete = false, writeFailed = false) }
        // S3: ReminderSync cancels the deleted habit's alarms here.
        write { deleteHabit(id) } ?: return
        emit(HabitEditorEffect.Close(CloseResult.DELETED))
    }

    /**
     * Runs a write. If it fails, the editor goes back to EDITING with the message on screen and
     * the draft untouched. The repository is domain code and does not expose Room or SQLite
     * exceptions, so Exception is caught; CancellationException is always rethrown.
     */
    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T : Any> write(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        reduce { copy(phase = HabitEditorState.Phase.EDITING, writeFailed = true) }
        null
    }
}
