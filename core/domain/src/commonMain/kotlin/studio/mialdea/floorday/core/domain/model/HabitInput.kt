package studio.mialdea.floorday.core.domain.model

import kotlinx.datetime.LocalTime

/**
 * What the editor hands to SaveHabitUseCase: the user's choices without ids, dates or
 * timestamps, which the use case owns. [id] null = new habit.
 *
 * [slots] are the slots in use, in the user order. A slot of the stored habit that is not
 * here is ended (removedOn), never dropped, so its history stays.
 */
data class HabitInput(
    val id: HabitId?,
    val name: String,
    val kind: HabitKind,
    val unitLabel: String?,
    val days: DaysOfWeek,
    val countsForMinimum: Boolean,
    val slots: List<SlotInput>,
)

/** [id] null = slot added in this edit. */
data class SlotInput(
    val id: SlotId?,
    val label: String,
    val time: LocalTime?,
    val amount: Quarters,
    val reminder: ReminderMode,
)

sealed interface SaveHabitResult {
    data class Saved(val habit: Habit) : SaveHabitResult

    /** The habit was deleted while it was being edited. */
    data object NotFound : SaveHabitResult
}
