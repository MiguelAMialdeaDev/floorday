package studio.mialdea.floorday.core.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.UserSettings

class DayStartTest {

    @Test
    fun `por defecto son las 04_00`() {
        assertEquals(240, DayStart.Default.minutesOfDay)
        assertEquals(LocalTime(4, 0), DayStart.Default.time)
        assertEquals("04:00", DayStart.Default.toString())
    }

    @Test
    fun `admite de 00_00 a 06_00`() {
        assertEquals(0, DayStart.ofMinutes(0).minutesOfDay)
        assertEquals(360, DayStart.ofMinutes(360).minutesOfDay)
        assertEquals(LocalTime(2, 30), DayStart.of(2, 30).time)
        assertFailsWith<IllegalArgumentException> { DayStart.ofMinutes(-1) }
        assertFailsWith<IllegalArgumentException> { DayStart.ofMinutes(361) }
        assertFailsWith<IllegalArgumentException> { DayStart.of(23) }
    }

    @Test
    fun `los minutos fuera de rango se rechazan`() {
        assertFailsWith<IllegalArgumentException> { DayStart.of(2, 75) }
        assertFailsWith<IllegalArgumentException> { DayStart.of(2, -1) }
    }

    @Test
    fun `un valor guardado invalido vuelve a las 04_00`() {
        assertEquals(DayStart.Default, DayStart.fromStoredOrDefault(null))
        assertEquals(DayStart.Default, DayStart.fromStoredOrDefault(-5))
        assertEquals(DayStart.Default, DayStart.fromStoredOrDefault(1_000))
        assertEquals(DayStart.of(5), DayStart.fromStoredOrDefault(300))
    }

    @Test
    fun `los ajustes delegan el inicio del dia en DayStart`() {
        val corrupt = UserSettings(dayStartMinutes = 1_000)

        val sanitized = corrupt.sanitized()

        assertEquals(DayStart.Default, corrupt.dayStart)
        assertEquals(DayStart.DEFAULT_MINUTES, sanitized.dayStartMinutes)
        assertEquals(DayStart.of(5), UserSettings(dayStartMinutes = 300).sanitized().dayStart)
    }
}
