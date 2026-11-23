package studio.mialdea.floorday.feature.today.editor

import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitInput
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.domain.model.SlotInput

/** Round trip between the domain and the editor draft. Pure. */
object HabitEditorMapper {

    /** Draft of a new habit, ready to edit. */
    fun newHabit(): HabitEditorState = HabitEditorState(phase = HabitEditorState.Phase.EDITING)

    /** Only the slots in use: ended ones keep their history and are not edited. */
    fun fromHabit(habit: Habit): HabitEditorState {
        val slots = habit.currentSlots.mapIndexed { index, slot ->
            SlotDraft(
                key = index,
                id = slot.id,
                label = slot.label,
                time = slot.time,
                amount = slot.amount,
                reminder = slot.reminder,
            )
        }
        return HabitEditorState(
            phase = HabitEditorState.Phase.EDITING,
            habitId = habit.id,
            name = habit.name,
            kind = habit.kind,
            slots = slots,
            days = habit.days,
            countsForMinimum = habit.countsForMinimum,
            unitLabel = habit.unitLabel.orEmpty(),
            nextKey = slots.size,
        )
    }

    /** What gets saved. Called with a validated draft; the kind decides amount, unit and reminder. */
    fun toInput(state: HabitEditorState): HabitInput = HabitInput(
        id = state.habitId,
        name = state.name,
        kind = state.kind,
        unitLabel = state.unitLabel.takeIf { state.isMedication },
        days = state.days,
        countsForMinimum = state.countsForMinimum,
        slots = state.slots.map { slot ->
            SlotInput(
                id = slot.id,
                label = slot.label,
                time = slot.time,
                amount = if (state.kind.allowsAmount) slot.amount else Quarters.ONE,
                reminder = if (slot.reminder == ReminderMode.INSISTENT && !state.kind.allowsInsistentReminder) {
                    ReminderMode.GENTLE
                } else {
                    slot.reminder
                },
            )
        },
    )
}
