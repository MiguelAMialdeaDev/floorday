package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.Fixtures.slot

class HabitSlotTest {

    @Test
    fun `el recordatorio insistente necesita hora`() {
        assertFailsWith<IllegalArgumentException> {
            slot(time = null, reminder = ReminderMode.INSISTENT)
        }
    }

    @Test
    fun `GENTLE y NONE funcionan sin hora`() {
        assertNull(slot(time = null, reminder = ReminderMode.GENTLE).time)
        assertNull(slot(time = null, reminder = ReminderMode.NONE).time)
    }

    @Test
    fun `la hora va en minutos exactos`() {
        assertFailsWith<IllegalArgumentException> { slot(time = LocalTime(8, 0, 30)) }
        assertFailsWith<IllegalArgumentException> { slot(time = LocalTime(8, 0, 0, 1)) }
    }

    @Test
    fun `la etiqueta puede estar vacia pero sin espacios en los bordes y con tope`() {
        assertEquals("", slot(label = "").label)
        assertEquals(HabitSlot.LABEL_MAX_LENGTH, slot(label = "x".repeat(HabitSlot.LABEL_MAX_LENGTH)).label.length)
        assertFailsWith<IllegalArgumentException> { slot(label = " Mañana") }
        assertFailsWith<IllegalArgumentException> {
            slot(label = "x".repeat(HabitSlot.LABEL_MAX_LENGTH + 1))
        }
    }

    @Test
    fun `un momento existe desde addedOn hasta el dia anterior a removedOn`() {
        val added = LocalDate(2026, 12, 7)
        val removed = LocalDate(2026, 12, 10)
        val ended = slot(addedOn = added, removedOn = removed)

        assertFalse(ended.isActiveOn(LocalDate(2026, 12, 6)))
        assertTrue(ended.isActiveOn(added))
        assertTrue(ended.isActiveOn(LocalDate(2026, 12, 9)))
        assertFalse(ended.isActiveOn(removed))
        assertFalse(ended.isCurrent)
    }

    @Test
    fun `sin fechas el momento existe siempre`() {
        val always = slot()

        assertTrue(always.isActiveOn(LocalDate(2000, 1, 1)))
        assertTrue(always.isActiveOn(LocalDate(2100, 1, 1)))
        assertTrue(always.isCurrent)
    }

    @Test
    fun `no se puede retirar un momento antes de anadirlo`() {
        assertFailsWith<IllegalArgumentException> {
            slot(addedOn = LocalDate(2026, 12, 10), removedOn = LocalDate(2026, 12, 9))
        }
    }
}
