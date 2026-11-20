package studio.mialdea.floorday.core.testing.builder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.testing.time.TestDates

class BuildersTest {

    @Test
    fun `el habito por defecto tiene un momento enlazado a el`() {
        val habit = aHabit()

        assertEquals(habit.id, habit.slots.single().habitId)
        assertEquals(HabitKind.CHECK, habit.kind)
        assertTrue(habit.countsForMinimum)
    }

    @Test
    fun `la medicacion por defecto es una toma insistente por la manana`() {
        val med = aMedication()

        assertEquals(HabitKind.MEDICATION, med.kind)
        assertEquals(ReminderMode.INSISTENT, med.slots.single().reminder)
    }

    @Test
    fun `el dia de ejemplo tiene cuatro momentos en el minimo y uno de medio`() {
        val slots = sampleMinimumDay().filter { it.countsForMinimum }.flatMap { it.slots }

        assertEquals(4, slots.size)
        assertTrue(slots.any { it.amount == Quarters.HALF })
    }

    @Test
    fun `aCompletion copia la cantidad del momento`() {
        val slot = sampleMinimumDay().first().slots.last()

        val completion = aCompletion(slot, TestDates.SPRINT0_END)

        assertEquals(Quarters.HALF, completion.amount)
        assertEquals(slot.id, completion.slotId)
    }

    @Test
    fun `uuidOf es determinista`() {
        assertEquals("00000000-0000-0000-0000-000000000001", uuidOf(1).toString())
        assertEquals(slotIdOf(2).value, uuidOf(2))
    }
}
