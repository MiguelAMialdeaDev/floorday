package studio.mialdea.floorday.feature.today.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent as I

class HabitEditorReducerTest {

    private val new = HabitEditorMapper.newHabit()
    private val medication = new.copy(kind = HabitKind.MEDICATION)

    private fun HabitEditorState.after(vararg intents: HabitEditorIntent): HabitEditorState =
        intents.fold(this) { state, intent -> HabitEditorReducer.reduce(state, intent) }

    @Test
    fun `mientras carga o guarda no cambia nada`() {
        val saving = new.copy(phase = HabitEditorState.Phase.SAVING)

        assertSame(saving, saving.after(I.NameChanged("Leer")))
    }

    @Test
    fun `los errores solo aparecen tras guardar y luego se revalidan en vivo`() {
        assertEquals(emptyList(), new.after(I.NameChanged("")).visibleErrors)

        val submitted = new.copy(submitted = true).after(I.NameChanged(""))
        assertEquals(listOf(EditorError.NameBlank), submitted.visibleErrors)

        assertEquals(emptyList(), submitted.after(I.NameChanged("Leer")).visibleErrors)
    }

    @Test
    fun `se pueden anadir hasta seis momentos y quitar hasta dejar uno`() {
        val full = new.after(*Array(Habit.MAX_SLOTS) { I.SlotAdded })
        assertEquals(Habit.MAX_SLOTS, full.slots.size)
        assertEquals(full.slots.map { it.key }.distinct(), full.slots.map { it.key })

        val one = full.after(*full.slots.map { I.SlotRemoved(it.key) }.toTypedArray())
        assertEquals(1, one.slots.size)
    }

    @Test
    fun `la cantidad va de un cuarto a dos y se queda en los extremos`() {
        val key = medication.slots[0].key

        val up = medication.after(*Array(10) { I.AmountIncreased(key) })
        assertEquals(Quarters(8), up.slot(key)?.amount)

        val down = up.after(*Array(10) { I.AmountDecreased(key) })
        assertEquals(Quarters.QUARTER, down.slot(key)?.amount)
    }

    @Test
    fun `elegir insistente sin hora abre el selector y quitar la hora lo baja a ninguno`() {
        val key = medication.slots[0].key

        val insistent = medication.after(I.ReminderChanged(key, ReminderMode.INSISTENT))
        assertEquals(key, insistent.timePickerSlot)

        val picked = insistent.after(I.TimePicked(key, LocalTime(10, 0)))
        assertNull(picked.timePickerSlot)
        assertEquals(LocalTime(10, 0), picked.slot(key)?.time)

        assertEquals(ReminderMode.NONE, picked.after(I.TimeCleared(key)).slot(key)?.reminder)
    }

    @Test
    fun `el suave no necesita hora ni abre el selector`() {
        val key = new.slots[0].key

        val gentle = new.after(I.ReminderChanged(key, ReminderMode.GENTLE), I.TimeCleared(key))

        assertNull(gentle.timePickerSlot)
        assertEquals(ReminderMode.GENTLE, gentle.slot(key)?.reminder)
    }

    @Test
    fun `un check no ofrece el recordatorio insistente`() {
        val key = new.slots[0].key

        assertSame(new, new.after(I.ReminderChanged(key, ReminderMode.INSISTENT)))
    }

    @Test
    fun `pasar a check baja el insistente a suave y conserva los momentos y cantidades`() {
        val key = medication.slots[0].key
        val med = medication.after(
            I.TimePicked(key, LocalTime(10, 0)),
            I.ReminderChanged(key, ReminderMode.INSISTENT),
            I.AmountDecreased(key),
            I.SlotAdded,
        )

        val check = med.after(I.KindChanged(HabitKind.CHECK))

        assertEquals(2, check.slots.size)
        assertEquals(ReminderMode.GENTLE, check.slot(key)?.reminder)
        assertEquals(Quarters.THREE_QUARTERS, check.after(I.KindChanged(HabitKind.MEDICATION)).slot(key)?.amount)
    }

    @Test
    fun `los dias se marcan y desmarcan y todos los dias los recupera`() {
        val noMonday = new.after(I.DayToggled(DayOfWeek.MONDAY))
        assertTrue(DayOfWeek.MONDAY !in noMonday.days)

        assertEquals(DaysOfWeek.EVERY_DAY, noMonday.after(I.EveryDaySelected).days)
    }

    @Test
    fun `borrar solo se ofrece al editar un habito guardado`() {
        assertSame(new, new.after(I.DeleteClicked))

        val stored = HabitEditorMapper.fromHabit(sampleHabit())
        assertTrue(stored.after(I.DeleteClicked).confirmingDelete)
    }
}
