package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
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
}
