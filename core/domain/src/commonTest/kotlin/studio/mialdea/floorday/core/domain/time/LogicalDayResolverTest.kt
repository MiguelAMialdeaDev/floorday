package studio.mialdea.floorday.core.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.testing.time.FakeClock
import studio.mialdea.floorday.core.testing.time.FakeTimeZoneProvider
import studio.mialdea.floorday.core.testing.time.TestZones

class LogicalDayResolverTest {

    private val clock = FakeClock(Instant.parse("2026-12-16T04:30:00Z")) // 05:30 en Madrid
    private val zones = FakeTimeZoneProvider(TestZones.MADRID)
    private val resolver = LogicalDayResolver(clock, zones)

    @Test
    fun `today usa el reloj y la zona inyectados`() {
        assertEquals(LocalDate(2026, 12, 16), resolver.today(DayStart.Default))

        clock.set(Instant.parse("2026-12-16T02:00:00Z")) // 03:00 en Madrid

        assertEquals(LocalDate(2026, 12, 15), resolver.today(DayStart.Default))
    }

    @Test
    fun `al cambiar de zona cambia el dia sin tocar el reloj`() {
        assertEquals(LocalDate(2026, 12, 16), resolver.today(DayStart.Default))

        zones.zone = TestZones.NEW_YORK // 23:30 del 15

        assertEquals(LocalDate(2026, 12, 15), resolver.today(DayStart.Default))
    }

    @Test
    fun `la zona se pide en cada llamada y nunca se guarda`() {
        resolver.today(DayStart.Default)
        resolver.dayOf(clock.now(), DayStart.Default)
        resolver.startOf(LocalDate(2026, 12, 16), DayStart.Default)
        resolver.endOf(LocalDate(2026, 12, 16), DayStart.Default)
        resolver.nextStart(DayStart.Default)

        assertEquals(5, zones.calls)
    }

    @Test
    fun `startOf y endOf delegan en la zona actual`() {
        val day = LocalDate(2026, 12, 16)

        assertEquals(Instant.parse("2026-12-16T03:00:00Z"), resolver.startOf(day, DayStart.Default))
        assertEquals(Instant.parse("2026-12-17T03:00:00Z"), resolver.endOf(day, DayStart.Default))
    }

    @Test
    fun `el ajuste de inicio del dia se aplica`() {
        clock.set(Instant.parse("2026-12-15T23:30:00Z")) // 00:30 del 16 en Madrid

        assertEquals(LocalDate(2026, 12, 15), resolver.today(DayStart.Default))
        assertEquals(LocalDate(2026, 12, 16), resolver.today(DayStart.of(0)))
    }

    @Test
    fun `nextStart es el fin del dia logico actual`() {
        // 05:30 del 16 en Madrid: el día 16 acaba a las 04:00 del 17.
        assertEquals(Instant.parse("2026-12-17T03:00:00Z"), resolver.nextStart(DayStart.Default))

        clock.set(Instant.parse("2026-12-16T02:59:00Z")) // 03:59 del 16, todavía es el día 15

        assertEquals(Instant.parse("2026-12-16T03:00:00Z"), resolver.nextStart(DayStart.Default))
    }

    @Test
    fun `nextStart avanza al cruzar el inicio del dia`() {
        clock.set(Instant.parse("2026-12-16T02:59:00Z"))
        val first = resolver.nextStart(DayStart.Default)

        clock.advanceBy(1.minutes) // 04:00 en Madrid

        assertEquals(first, resolver.startOf(LocalDate(2026, 12, 16), DayStart.Default))
        assertEquals(Instant.parse("2026-12-17T03:00:00Z"), resolver.nextStart(DayStart.Default))
    }

    @Test
    fun `nextStart respeta el dia corto de marzo`() {
        clock.set(Instant.parse("2027-03-27T12:00:00Z")) // sábado 27, día lógico de 23 h

        assertEquals(Instant.parse("2027-03-28T02:00:00Z"), resolver.nextStart(DayStart.Default))
    }
}
