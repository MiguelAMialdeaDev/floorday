package studio.mialdea.floorday.core.testing.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime

class FakeClockTest {

    @Test
    fun `advanceBy avanza el reloj`() {
        val clock = FakeClock(Instant.parse("2026-12-01T07:00:00Z"))

        clock.advanceBy(90.minutes)

        assertEquals(Instant.parse("2026-12-01T08:30:00Z"), clock.now())
    }

    @Test
    fun `advanceBy rechaza duraciones negativas`() {
        assertFailsWith<IllegalArgumentException> { FakeClock().advanceBy((-1).seconds) }
    }

    @Test
    fun `setLocal usa la zona indicada`() {
        val clock = FakeClock()

        clock.setLocal(LocalDate(2026, 12, 6), LocalTime(23, 30), TestZones.MADRID)

        assertEquals(Instant.parse("2026-12-06T22:30:00Z"), clock.now())
    }

    @Test
    fun `en el hueco de marzo se usa el offset anterior al salto`() {
        val instant = instantOf(TestDates.DST_SPRING_2027, LocalTime(2, 30))

        assertEquals(Instant.parse("2027-03-28T01:30:00Z"), instant)
        assertEquals(LocalDateTime(2027, 3, 28, 3, 30), instant.toLocalDateTime(TestZones.MADRID))
    }

    @Test
    fun `en el solape de octubre se toma la primera ocurrencia`() {
        assertEquals(
            Instant.parse("2027-10-31T00:30:00Z"),
            instantOf(TestDates.DST_AUTUMN_2027, LocalTime(2, 30)),
        )
    }

    @Test
    fun `FakeTimeZoneProvider devuelve la zona actual y cuenta las llamadas`() {
        val zones = FakeTimeZoneProvider()
        zones.current()

        zones.zone = TestZones.NEW_YORK

        assertEquals(TestZones.NEW_YORK, zones.current())
        assertEquals(2, zones.calls)
    }
}
