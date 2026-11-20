package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.uuid.Uuid

class IdsTest {

    @Test
    fun `parse y toString hacen ida y vuelta`() {
        val text = "550e8400-e29b-41d4-a716-446655440000"

        assertEquals(text, HabitId.parse(text).toString())
        assertEquals(text, SlotId.parse(text).toString())
    }

    @Test
    fun `parse no distingue mayusculas`() {
        assertEquals(
            HabitId.parse("550e8400-e29b-41d4-a716-446655440000"),
            HabitId.parse("550E8400-E29B-41D4-A716-446655440000"),
        )
    }

    @Test
    fun `parse rechaza un texto que no es un uuid`() {
        assertFailsWith<IllegalArgumentException> { HabitId.parse("no es un uuid") }
    }

    @Test
    fun `el generador se puede inyectar`() {
        val fixed = Uuid.parse("00000000-0000-4000-8000-0000000000aa")
        val generator = IdGenerator { fixed }

        assertEquals(HabitId(fixed), generator.newHabitId())
        assertEquals(SlotId(fixed), generator.newSlotId())
    }

    @Test
    fun `el generador aleatorio da ids distintos`() {
        val ids = List(100) { IdGenerator.Random.newHabitId() }.toSet()

        assertEquals(100, ids.size)
    }
}
