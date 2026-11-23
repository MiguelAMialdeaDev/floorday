package studio.mialdea.floorday.feature.today.editor

import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitSlot

/**
 * Editor rules, pure: same state, same errors, in screen order (top to bottom), so the first
 * one is where focus goes. The limits are the domain's, so a valid draft always builds a Habit.
 */
object HabitEditorValidator {

    fun validate(state: HabitEditorState): List<EditorError> = buildList {
        val name = state.name.trim()
        when {
            name.isEmpty() -> add(EditorError.NameBlank)
            name.length > Habit.NAME_MAX_LENGTH -> add(EditorError.NameTooLong)
        }
        if (state.isMedication && state.unitLabel.trim().length > Habit.UNIT_LABEL_MAX_LENGTH) {
            add(EditorError.UnitTooLong)
        }

        val timesInUse = state.slots.mapNotNull { it.time }.groupingBy { it }.eachCount()
        state.slots.forEach { slot ->
            if (slot.label.trim().length > HabitSlot.LABEL_MAX_LENGTH) add(EditorError.LabelTooLong(slot.key))
            if (slot.time != null && timesInUse.getValue(slot.time) > 1) add(EditorError.DuplicateTime(slot.key))
            if (slot.reminder.requiresTime && slot.time == null) add(EditorError.ReminderNeedsTime(slot.key))
        }

        if (state.days.isEmpty) add(EditorError.NoDays)
    }
}
