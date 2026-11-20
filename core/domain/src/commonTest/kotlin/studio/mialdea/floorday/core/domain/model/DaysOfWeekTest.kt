package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek

class DaysOfWeekTest {

    @Test
    fun `el bitmask empieza en lunes y es estable`() {
        // Contract with the DB and the JSON: do not change without a migration.
        assertEquals(1, DaysOfWeek.of(DayOfWeek.MONDAY).mask)
        assertEquals(2, DaysOfWeek.of(DayOfWeek.TUESDAY).mask)
        assertEquals(64, DaysOfWeek.of(DayOfWeek.SUNDAY).mask)
        assertEquals(127, DaysOfWeek.EVERY_DAY.mask)
        assertEquals(31, DaysOfWeek.WEEKDAYS.mask)
    }

    @Test
    fun `EVERY_DAY contiene los siete dias`() {
        assertEquals(7, DaysOfWeek.EVERY_DAY.size)
        DayOfWeek.entries.forEach { assertTrue(it in DaysOfWeek.EVERY_DAY) }
    }

    @Test
    fun `NONE esta vacio`() {
        assertTrue(DaysOfWeek.NONE.isEmpty)
        assertEquals(0, DaysOfWeek.NONE.size)
        assertEquals(emptySet<DayOfWeek>(), DaysOfWeek.NONE.toSet())
    }

    @Test
    fun `solo contiene los dias dados`() {
        val days = DaysOfWeek.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)

        assertTrue(DayOfWeek.MONDAY in days)
        assertTrue(DayOfWeek.WEDNESDAY in days)
        assertFalse(DayOfWeek.TUESDAY in days)
        assertEquals(2, days.size)
    }

    @Test
    fun `los laborables excluyen el fin de semana`() {
        assertFalse(DayOfWeek.SATURDAY in DaysOfWeek.WEEKDAYS)
        assertFalse(DayOfWeek.SUNDAY in DaysOfWeek.WEEKDAYS)
        assertEquals(5, DaysOfWeek.WEEKDAYS.size)
    }

    @Test
    fun `plus minus y toggle se comportan como un conjunto`() {
        val start = DaysOfWeek.of(DayOfWeek.MONDAY)

        val added = start + DayOfWeek.FRIDAY

        assertEquals(DaysOfWeek.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), added)
        assertEquals(start, added - DayOfWeek.FRIDAY)
        assertEquals(start, start + DayOfWeek.MONDAY)
        assertEquals(DaysOfWeek.NONE, start.toggle(DayOfWeek.MONDAY))
        assertEquals(start, DaysOfWeek.NONE.toggle(DayOfWeek.MONDAY))
    }

    @Test
    fun `toSet sigue el orden ISO`() {
        val days = DaysOfWeek.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.THURSDAY)

        assertEquals(
            listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
            days.toSet().toList(),
        )
    }

    @Test
    fun `un conjunto hace ida y vuelta`() {
        val set = setOf(DayOfWeek.TUESDAY, DayOfWeek.SATURDAY)

        assertEquals(set, DaysOfWeek.of(set).toSet())
    }

    @Test
    fun `rechaza un bitmask invalido`() {
        assertFailsWith<IllegalArgumentException> { DaysOfWeek(128) }
        assertFailsWith<IllegalArgumentException> { DaysOfWeek(-1) }
    }
}
