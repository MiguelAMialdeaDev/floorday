package studio.mialdea.floorday.feature.today.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.ReminderMode

class HabitEditorValidatorTest {

    private val valid = HabitEditorMapper.newHabit().copy(name = "Leer")

    @Test
    fun `un borrador correcto no tiene errores`() {
        assertEquals(emptyList(), HabitEditorValidator.validate(valid))
    }

    @Test
    fun `el nombre no puede quedar vacio ni pasar de cuarenta caracteres`() {
        assertEquals(listOf(EditorError.NameBlank), HabitEditorValidator.validate(valid.copy(name = "   ")))
        assertEquals(listOf(EditorError.NameTooLong), HabitEditorValidator.validate(valid.copy(name = "a".repeat(41))))
        assertEquals(emptyList(), HabitEditorValidator.validate(valid.copy(name = " ${"a".repeat(40)} ")))
    }

    @Test
    fun `la unidad larga solo cuenta en medicacion`() {
        val longUnit = valid.copy(unitLabel = "u".repeat(17))

        assertEquals(emptyList(), HabitEditorValidator.validate(longUnit))
        assertEquals(listOf(EditorError.UnitTooLong), HabitEditorValidator.validate(longUnit.copy(kind = HabitKind.MEDICATION)))
    }

    @Test
    fun `dos momentos a la misma hora marcan los dos`() {
        val state = valid.copy(
            slots = listOf(SlotDraft(key = 0, time = LocalTime(10, 0)), SlotDraft(key = 1, time = LocalTime(10, 0))),
        )

        assertEquals(listOf(EditorError.DuplicateTime(0), EditorError.DuplicateTime(1)), HabitEditorValidator.validate(state))
    }

    @Test
    fun `el insistente necesita hora y el suave no`() {
        val state = valid.copy(
            kind = HabitKind.MEDICATION,
            slots = listOf(
                SlotDraft(key = 0, reminder = ReminderMode.INSISTENT),
                SlotDraft(key = 1, reminder = ReminderMode.GENTLE),
            ),
        )

        assertEquals(listOf(EditorError.ReminderNeedsTime(0)), HabitEditorValidator.validate(state))
    }

    @Test
    fun `los errores salen en el orden de la pantalla`() {
        val state = valid.copy(
            name = "",
            slots = listOf(SlotDraft(key = 0, label = "e".repeat(25))),
            days = DaysOfWeek.NONE,
        )

        assertEquals(
            listOf(EditorError.NameBlank, EditorError.LabelTooLong(0), EditorError.NoDays),
            HabitEditorValidator.validate(state),
        )
    }
}
