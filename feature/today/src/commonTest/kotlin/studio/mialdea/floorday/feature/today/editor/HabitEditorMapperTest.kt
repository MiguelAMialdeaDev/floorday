package studio.mialdea.floorday.feature.today.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.domain.model.SlotInput
import studio.mialdea.floorday.core.testing.builder.slotIdOf

class HabitEditorMapperTest {

    @Test
    fun `un habito guardado se edita con sus momentos vigentes y vuelve igual`() {
        val habit = sampleHabit()

        val input = HabitEditorMapper.toInput(HabitEditorMapper.fromHabit(habit))

        assertEquals(habit.id, input.id)
        assertEquals(habit.name to habit.unitLabel, input.name to input.unitLabel)
        assertEquals(
            habit.currentSlots.map { SlotInput(it.id, it.label, it.time, it.amount, it.reminder) },
            input.slots,
        )
    }

    @Test
    fun `los momentos retirados no se editan`() {
        val habit = sampleHabit().let { it.copy(slots = listOf(it.slots[0], it.slots[1].copy(removedOn = LocalDate(2026, 11, 1)))) }

        val state = HabitEditorMapper.fromHabit(habit)

        assertEquals(listOf(slotIdOf(1)), state.slots.map { it.id })
        assertEquals(1, state.nextKey)
    }

    @Test
    fun `como check se guarda sin unidad cantidad ni insistente`() {
        val state = HabitEditorMapper.fromHabit(sampleHabit()).copy(kind = HabitKind.CHECK)

        val input = HabitEditorMapper.toInput(state)

        assertNull(input.unitLabel)
        assertEquals(listOf(Quarters.ONE, Quarters.ONE), input.slots.map { it.amount })
        assertEquals(listOf(ReminderMode.GENTLE, ReminderMode.NONE), input.slots.map { it.reminder })
    }

    @Test
    fun `un momento nuevo sale sin id`() {
        val state = HabitEditorReducer.reduce(HabitEditorMapper.fromHabit(sampleHabit()), HabitEditorIntent.SlotAdded)

        assertNull(HabitEditorMapper.toInput(state).slots.last().id)
    }
}
