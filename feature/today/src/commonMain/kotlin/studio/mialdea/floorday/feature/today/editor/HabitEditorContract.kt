package studio.mialdea.floorday.feature.today.editor

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.domain.model.SlotId

/**
 * Draft of a slot. [key] identifies it inside the editor, also before it has an id; [id] is null
 * for a slot added in this edit (the save use case gives it one).
 */
data class SlotDraft(
    val key: Int,
    val id: SlotId? = null,
    val label: String = "",
    val time: LocalTime? = null,
    val amount: Quarters = Quarters.ONE,
    val reminder: ReminderMode = ReminderMode.NONE,
)

data class HabitEditorState(
    val phase: Phase = Phase.LOADING,
    /** null = new habit. */
    val habitId: HabitId? = null,
    val name: String = "",
    val kind: HabitKind = HabitKind.CHECK,
    val slots: List<SlotDraft> = listOf(SlotDraft(key = 0)),
    val days: DaysOfWeek = DaysOfWeek.EVERY_DAY,
    val countsForMinimum: Boolean = true,
    val unitLabel: String = "",
    /** Errors show after the first Save and are then checked again on every change. */
    val submitted: Boolean = false,
    val errors: List<EditorError> = emptyList(),
    /** Slot whose time picker is open. */
    val timePickerSlot: Int? = null,
    val confirmingDelete: Boolean = false,
    /** The last write failed. The draft stays as it was so Save can be tried again. */
    val writeFailed: Boolean = false,
    /** Key for the next slot added. */
    val nextKey: Int = 1,
) {
    enum class Phase { LOADING, EDITING, SAVING }

    val isNew: Boolean get() = habitId == null

    val isMedication: Boolean get() = kind == HabitKind.MEDICATION

    val canAddSlot: Boolean get() = slots.size < Habit.MAX_SLOTS

    val canRemoveSlot: Boolean get() = slots.size > 1

    val canDelete: Boolean get() = !isNew && phase == Phase.EDITING

    /** The insistent reminder is for medication only. */
    val reminderOptions: List<ReminderMode>
        get() = if (isMedication) ReminderMode.entries else listOf(ReminderMode.NONE, ReminderMode.GENTLE)

    val visibleErrors: List<EditorError> get() = if (submitted) errors else emptyList()

    fun error(field: EditorField): EditorError? = visibleErrors.firstOrNull { it.field == field && it.slot == null }

    fun errorsFor(slot: Int): List<EditorError> = visibleErrors.filter { it.slot == slot }

    fun slot(key: Int): SlotDraft? = slots.firstOrNull { it.key == key }
}

enum class EditorField { NAME, UNIT, SLOTS, DAYS }

sealed interface EditorError {
    val field: EditorField

    /** Key of the slot the error belongs to, or null for a habit field. */
    val slot: Int? get() = null

    data object NameBlank : EditorError {
        override val field get() = EditorField.NAME
    }

    data object NameTooLong : EditorError {
        override val field get() = EditorField.NAME
    }

    data object UnitTooLong : EditorError {
        override val field get() = EditorField.UNIT
    }

    data class LabelTooLong(override val slot: Int) : EditorError {
        override val field get() = EditorField.SLOTS
    }

    data class DuplicateTime(override val slot: Int) : EditorError {
        override val field get() = EditorField.SLOTS
    }

    data class ReminderNeedsTime(override val slot: Int) : EditorError {
        override val field get() = EditorField.SLOTS
    }

    data object NoDays : EditorError {
        override val field get() = EditorField.DAYS
    }
}

sealed interface HabitEditorIntent {
    data class NameChanged(val value: String) : HabitEditorIntent
    data class KindChanged(val kind: HabitKind) : HabitEditorIntent
    data class UnitChanged(val value: String) : HabitEditorIntent
    data class DayToggled(val day: DayOfWeek) : HabitEditorIntent
    data object EveryDaySelected : HabitEditorIntent
    data class CountsForMinimumChanged(val value: Boolean) : HabitEditorIntent

    data object SlotAdded : HabitEditorIntent
    data class SlotRemoved(val slot: Int) : HabitEditorIntent
    data class LabelChanged(val slot: Int, val value: String) : HabitEditorIntent
    data class AmountIncreased(val slot: Int) : HabitEditorIntent
    data class AmountDecreased(val slot: Int) : HabitEditorIntent
    data class ReminderChanged(val slot: Int, val mode: ReminderMode) : HabitEditorIntent

    data class TimePickerOpened(val slot: Int) : HabitEditorIntent
    data class TimePicked(val slot: Int, val time: LocalTime) : HabitEditorIntent
    data class TimeCleared(val slot: Int) : HabitEditorIntent
    data object TimePickerDismissed : HabitEditorIntent

    data object SaveClicked : HabitEditorIntent
    data object DeleteClicked : HabitEditorIntent
    data object DeleteConfirmed : HabitEditorIntent
    data object DeleteDismissed : HabitEditorIntent
    data object CancelClicked : HabitEditorIntent
}

sealed interface HabitEditorEffect {
    data class Close(val result: CloseResult) : HabitEditorEffect

    /** Move focus to the first field with an error, so TalkBack reads it. */
    data class FocusError(val field: EditorField) : HabitEditorEffect
}

enum class CloseResult { SAVED, DELETED, CANCELLED, NOT_FOUND }
