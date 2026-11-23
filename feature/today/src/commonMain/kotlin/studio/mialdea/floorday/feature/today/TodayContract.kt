package studio.mialdea.floorday.feature.today

import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.designsystem.component.CheckState
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.SlotId

enum class DayPart { MORNING, AFTERNOON, NIGHT, ANYTIME }

/** A row as the screen draws it: texts already formatted, no domain objects. */
data class TodayRow(
    val slotId: SlotId,
    /** Habit the slot belongs to: a long press opens its editor. */
    val habitId: HabitId,
    val title: String,
    /** Slot label (only if the habit has several), time and amount, joined with " · ". */
    val supportingText: String?,
    /** Time of the mark, shown as "Done 08:42". Only when done. */
    val doneTime: String?,
    val state: CheckState,
    val partOfMinimum: Boolean,
)

data class TodaySection(val part: DayPart, val rows: List<TodayRow>)

data class TodayContent(
    val day: LocalDate,
    /** Rows of the Minimum Day grouped by part of the day. */
    val sections: List<TodaySection>,
    /** Habits outside the minimum, folded under "Other habits". */
    val others: List<TodayRow>,
    val hasHabits: Boolean,
) {
    private val minimumRows: List<TodayRow> get() = sections.flatMap { it.rows }

    // Counted from the rows so an optimistic tap moves the ring at once. The rows come from
    // MinimumDayEvaluator's sets, so the numbers match it (TodayMapperTest checks it).
    val done: Int get() = minimumRows.count { it.state == CheckState.Done }
    val required: Int get() = minimumRows.count { it.state != CheckState.Skipped }
    val isRestDay: Boolean get() = hasHabits && required == 0

    fun findRow(slotId: SlotId): TodayRow? =
        minimumRows.firstOrNull { it.slotId == slotId } ?: others.firstOrNull { it.slotId == slotId }

    fun withState(slotId: SlotId, state: CheckState): TodayContent {
        fun List<TodayRow>.patch() = map { if (it.slotId == slotId) it.copy(state = state) else it }
        return copy(sections = sections.map { it.copy(rows = it.rows.patch()) }, others = others.patch())
    }
}

/** What undo needs: the mark the slot had before the tap ([previous] null = it had none). */
data class UndoToken(val id: Long, val slotId: SlotId, val day: LocalDate, val previous: Completion?)

data class TodayState(
    val isLoading: Boolean = true,
    val content: TodayContent? = null,
    val othersExpanded: Boolean = false,
    val pendingUndo: UndoToken? = null,
)

sealed interface TodayIntent {
    data class ToggleSlot(val slotId: SlotId) : TodayIntent
    data class Undo(val undoId: Long) : TodayIntent
    data class UndoDismissed(val undoId: Long) : TodayIntent
    data object ToggleOthers : TodayIntent

    /** ON_RESUME: the clock may have moved while the app was in the background. */
    data object Resumed : TodayIntent
}

sealed interface TodayEffect {
    data class ShowUndo(val undoId: Long, val habitName: String, val markedDone: Boolean) : TodayEffect
    data object ShowSaveError : TodayEffect
}

/** Internal events of the reducer. The UI only knows [TodayIntent]. */
sealed interface TodayEvent {
    data class ContentLoaded(val content: TodayContent) : TodayEvent
    data class RowStateChanged(val slotId: SlotId, val state: CheckState) : TodayEvent
    data class UndoArmed(val token: UndoToken) : TodayEvent
    data class UndoCleared(val undoId: Long) : TodayEvent
    data object OthersToggled : TodayEvent
}
