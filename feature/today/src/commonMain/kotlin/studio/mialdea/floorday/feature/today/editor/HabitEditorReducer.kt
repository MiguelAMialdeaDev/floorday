package studio.mialdea.floorday.feature.today.editor

import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
// The alias hides the original name in this file, so both imports are needed.
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent as I

/**
 * Pure reducer of the editor. Intents with side effects (save, delete, cancel) are the
 * ViewModel's and leave the state as it is here. Only reduces while EDITING.
 */
object HabitEditorReducer {

    fun reduce(state: HabitEditorState, intent: HabitEditorIntent): HabitEditorState {
        if (state.phase != HabitEditorState.Phase.EDITING) return state
        val next = when (intent) {
            is I.NameChanged -> state.copy(name = intent.value)
            is I.KindChanged -> changeKind(state, intent.kind)
            is I.UnitChanged -> state.copy(unitLabel = intent.value)
            is I.DayToggled -> state.copy(days = state.days.toggle(intent.day))
            I.EveryDaySelected -> state.copy(days = DaysOfWeek.EVERY_DAY)
            is I.CountsForMinimumChanged -> state.copy(countsForMinimum = intent.value)

            I.SlotAdded -> addSlot(state)
            is I.SlotRemoved -> removeSlot(state, intent.slot)
            is I.LabelChanged -> state.updateSlot(intent.slot) { copy(label = intent.value) }
            is I.AmountIncreased -> state.updateSlot(intent.slot) { copy(amount = amount.step(+1)) }
            is I.AmountDecreased -> state.updateSlot(intent.slot) { copy(amount = amount.step(-1)) }
            is I.ReminderChanged -> changeReminder(state, intent.slot, intent.mode)

            is I.TimePickerOpened -> if (state.slot(intent.slot) != null) state.copy(timePickerSlot = intent.slot) else state
            is I.TimePicked -> state.updateSlot(intent.slot) { copy(time = intent.time) }.copy(timePickerSlot = null)
            // Without a time an insistent reminder has nothing to ring at.
            is I.TimeCleared -> state.updateSlot(intent.slot) {
                copy(time = null, reminder = if (reminder.requiresTime) ReminderMode.NONE else reminder)
            }.copy(timePickerSlot = null)
            I.TimePickerDismissed -> state.copy(timePickerSlot = null)

            I.DeleteClicked -> if (state.canDelete) state.copy(confirmingDelete = true) else state
            I.DeleteDismissed -> state.copy(confirmingDelete = false)

            I.SaveClicked, I.DeleteConfirmed, I.CancelClicked -> state
        }
        return if (next.submitted) next.copy(errors = HabitEditorValidator.validate(next)) else next
    }

    /**
     * A check has no amount, unit or insistent reminder. The amounts are kept in the draft (the
     * mapper saves 1 for a check), so going back to medication does not lose them; an insistent
     * reminder becomes gentle, since a check cannot show it.
     */
    private fun changeKind(state: HabitEditorState, kind: HabitKind): HabitEditorState {
        if (kind == state.kind) return state
        val slots = if (kind.allowsInsistentReminder) {
            state.slots
        } else {
            state.slots.map { if (it.reminder == ReminderMode.INSISTENT) it.copy(reminder = ReminderMode.GENTLE) else it }
        }
        return state.copy(kind = kind, slots = slots)
    }

    private fun addSlot(state: HabitEditorState): HabitEditorState {
        if (!state.canAddSlot) return state
        return state.copy(slots = state.slots + SlotDraft(key = state.nextKey), nextKey = state.nextKey + 1)
    }

    private fun removeSlot(state: HabitEditorState, key: Int): HabitEditorState {
        if (!state.canRemoveSlot || state.slot(key) == null) return state
        return state.copy(
            slots = state.slots.filterNot { it.key == key },
            timePickerSlot = state.timePickerSlot.takeUnless { it == key },
        )
    }

    private fun changeReminder(state: HabitEditorState, key: Int, mode: ReminderMode): HabitEditorState {
        if (mode !in state.reminderOptions) return state
        val updated = state.updateSlot(key) { copy(reminder = mode) }
        val slot = updated.slot(key) ?: return state
        // An insistent reminder needs a time: open the picker straight away.
        return if (mode.requiresTime && slot.time == null) updated.copy(timePickerSlot = key) else updated
    }

    private inline fun HabitEditorState.updateSlot(key: Int, block: SlotDraft.() -> SlotDraft): HabitEditorState {
        if (slot(key) == null) return this
        return copy(slots = slots.map { if (it.key == key) it.block() else it })
    }

    /** Next or previous amount the editor offers (¼ to 2); stays put at either end. */
    private fun Quarters.step(by: Int): Quarters {
        val choices = Quarters.EDITOR_CHOICES
        val index = choices.indexOf(this).takeIf { it >= 0 } ?: choices.indexOfLast { it < this }
        return choices[(index + by).coerceIn(0, choices.lastIndex)]
    }
}
