package studio.mialdea.floorday.core.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import studio.mialdea.floorday.core.testing.time.TestZones

/**
 * Europe/Madrid: CET = UTC+1, CEST = UTC+2.
 * 28-Mar-2027: at 01:00Z the clock jumps from 02:00 to 03:00 (gap 02:00-02:59).
 * 31-Oct-2027: at 01:00Z the clock goes back from 03:00 to 02:00 (overlap 02:00-02:59).
 * All instants are written in UTC with the local time next to them.
 */
class LogicalDayCalculatorTest {

    private val madrid = TestZones.MADRID
    private val four = DayStart.Default

    private fun utc(iso: String) = Instant.parse(iso)
    private fun dayOf(iso: String, start: DayStart = four, zone: TimeZone = madrid) =
        LogicalDayCalculator.dayOf(utc(iso), zone, start)

    // Midnight and the 04:00 boundary

    @Test
    fun `medianoche local sigue siendo el dia anterior con inicio a las 04`() {
        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-15T23:00:00Z")) // 00:00 on the 16th
    }

    @Test
    fun `23_59 local es el mismo dia`() {
        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-15T22:59:00Z")) // 23:59 on the 15th
    }

    @Test
    fun `03_59_59 es el dia anterior y 04_00 el nuevo`() {
        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-16T02:59:00Z")) // 03:59
        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-16T02:59:59.999Z")) // 03:59:59.999
        assertEquals(LocalDate(2026, 12, 16), dayOf("2026-12-16T03:00:00Z")) // 04:00
    }

    @Test
    fun `la media pastilla de las 23_30 confirmada a las 00_10 cuenta para su dia`() {
        val scheduled = dayOf("2026-12-15T22:30:00Z") // 23:30 on the 15th

        val confirmed = dayOf("2026-12-15T23:10:00Z") // 00:10 on the 16th

        assertEquals(scheduled, confirmed)
    }

    @Test
    fun `con inicio a las 00_00 el dia logico es el dia de calendario`() {
        val midnight = DayStart.of(0)

        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-15T22:59:59Z", midnight))
        assertEquals(LocalDate(2026, 12, 16), dayOf("2026-12-15T23:00:00Z", midnight))
    }

    @Test
    fun `un dia normal dura 24 h`() {
        assertEquals(24.hours, LogicalDayCalculator.lengthOf(LocalDate(2026, 12, 15), madrid, four))
    }

    // March clock change (23 h day)

    @Test
    fun `marzo 2027 con inicio a las 04 el dia corto es el sabado 27`() {
        assertEquals(23.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 3, 27), madrid, four))
        assertEquals(24.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 3, 28), madrid, four))
        assertEquals(utc("2027-03-27T03:00:00Z"), LogicalDayCalculator.startOf(LocalDate(2027, 3, 27), madrid, four))
        assertEquals(utc("2027-03-28T02:00:00Z"), LogicalDayCalculator.startOf(LocalDate(2027, 3, 28), madrid, four))
    }

    @Test
    fun `marzo 2027 frontera de las 04_00 en horario de verano`() {
        assertEquals(LocalDate(2027, 3, 27), dayOf("2027-03-28T00:59:59Z")) // 01:59:59 CET
        assertEquals(LocalDate(2027, 3, 27), dayOf("2027-03-28T01:00:00Z")) // 03:00 CEST, after the jump
        assertEquals(LocalDate(2027, 3, 27), dayOf("2027-03-28T01:59:59Z")) // 03:59:59 CEST
        assertEquals(LocalDate(2027, 3, 28), dayOf("2027-03-28T02:00:00Z")) // 04:00 CEST
    }

    @Test
    fun `marzo 2027 con inicio a las 00_00 el domingo 28 dura 23 h`() {
        assertEquals(23.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 3, 28), madrid, DayStart.of(0)))
    }

    @Test
    fun `restar 4 h al instante daria otro dia pero el reloj de pared no`() {
        // 04:30 CEST on the 28th = 02:30Z. Minus 4 h of real time: 22:30Z on the 27th = 23:30 CET (wrong).
        assertEquals(LocalDate(2027, 3, 28), dayOf("2027-03-28T02:30:00Z"))
    }

    // Nonexistent 02:30 (gap)

    @Test
    fun `inicio a las 02_30 en el hueco de marzo empieza en el instante del salto`() {
        val start = DayStart.of(2, 30)
        val day = LocalDate(2027, 3, 28)

        // toInstant would return 01:30Z (03:30 CEST) and lose half an hour of the day.
        assertEquals(utc("2027-03-28T01:30:00Z"), day.atTime(start.time).toInstant(madrid))
        assertEquals(utc("2027-03-28T01:00:00Z"), LogicalDayCalculator.startOf(day, madrid, start))
        assertEquals(LocalDate(2027, 3, 27), dayOf("2027-03-28T00:59:59Z", start)) // 01:59:59 CET
        assertEquals(LocalDate(2027, 3, 28), dayOf("2027-03-28T01:00:00Z", start)) // 03:00 CEST
        assertEquals(LocalDate(2027, 3, 28), dayOf("2027-03-28T01:15:00Z", start)) // 03:15 CEST
    }

    @Test
    fun `con inicio a las 02_30 los dias de alrededor del hueco duran 23 h y media`() {
        val start = DayStart.of(2, 30)

        assertEquals(23.hours + 30.minutes, LogicalDayCalculator.lengthOf(LocalDate(2027, 3, 27), madrid, start))
        assertEquals(23.hours + 30.minutes, LogicalDayCalculator.lengthOf(LocalDate(2027, 3, 28), madrid, start))
    }

    // October clock change (25 h day)

    @Test
    fun `octubre 2027 con inicio a las 04 el dia largo es el sabado 30`() {
        assertEquals(25.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 10, 30), madrid, four))
        assertEquals(24.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 10, 31), madrid, four))
        assertEquals(utc("2027-10-31T03:00:00Z"), LogicalDayCalculator.startOf(LocalDate(2027, 10, 31), madrid, four))
    }

    @Test
    fun `octubre 2027 las dos 02_30 y las 03_30 siguen en el dia 30`() {
        assertEquals(LocalDate(2027, 10, 30), dayOf("2027-10-31T00:30:00Z")) // 02:30 CEST
        assertEquals(LocalDate(2027, 10, 30), dayOf("2027-10-31T01:30:00Z")) // 02:30 CET
        assertEquals(LocalDate(2027, 10, 30), dayOf("2027-10-31T02:30:00Z")) // 03:30 CET
        assertEquals(LocalDate(2027, 10, 30), dayOf("2027-10-31T02:59:59Z")) // 03:59:59 CET
        assertEquals(LocalDate(2027, 10, 31), dayOf("2027-10-31T03:00:00Z")) // 04:00 CET
    }

    @Test
    fun `octubre 2027 con inicio a las 00_00 el domingo 31 dura 25 h`() {
        assertEquals(25.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 10, 31), madrid, DayStart.of(0)))
    }

    @Test
    fun `inicio a las 02_30 en el solape usa la primera ocurrencia y no retrocede`() {
        val start = DayStart.of(2, 30)

        assertEquals(utc("2027-10-31T00:30:00Z"), LogicalDayCalculator.startOf(LocalDate(2027, 10, 31), madrid, start))
        assertEquals(LocalDate(2027, 10, 30), dayOf("2027-10-31T00:29:59Z", start)) // 02:29:59 CEST
        assertEquals(LocalDate(2027, 10, 31), dayOf("2027-10-31T00:30:00Z", start)) // 02:30 CEST
        assertEquals(LocalDate(2027, 10, 31), dayOf("2027-10-31T01:15:00Z", start)) // 02:15 CET, second pass
        assertEquals(LocalDate(2027, 10, 31), dayOf("2027-10-31T01:30:00Z", start)) // 02:30 CET
        assertEquals(25.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 10, 31), madrid, start))
    }

    // Property: minute-by-minute sweep around the changes

    @Test
    fun `barrido de los cambios de 2026 y 2027 el dia es monotono y coherente con startOf`() {
        val starts = listOf(0, 2, 3, 4, 6).map { DayStart.of(it) } + DayStart.of(2, 30) + DayStart.of(3, 59)
        val windows = listOf(
            "2026-03-27T00:00:00Z", // change of 29-Mar-2026
            "2026-10-23T00:00:00Z", // change of 25-Oct-2026
            "2027-03-26T00:00:00Z",
            "2027-10-29T00:00:00Z",
        )
        for (start in starts) for (from in windows) {
            var instant = utc(from)
            val until = instant + 96.hours
            var previous = LogicalDayCalculator.dayOf(instant, madrid, start)
            while (instant < until) {
                val day = LogicalDayCalculator.dayOf(instant, madrid, start)
                assertTrue(day == previous || day == previous.plus(1, DateTimeUnit.DAY), "salto en $instant con $start")
                assertTrue(LogicalDayCalculator.startOf(day, madrid, start) <= instant, "startOf > $instant con $start")
                assertTrue(instant < LogicalDayCalculator.endOf(day, madrid, start), "endOf <= $instant con $start")
                previous = day
                instant += 1.minutes
            }
        }
    }

    // Zone change while travelling

    @Test
    fun `el mismo instante es otro dia logico en otra zona`() {
        val instant = "2026-12-16T04:30:00Z"

        assertEquals(LocalDate(2026, 12, 16), dayOf(instant, zone = madrid)) // 05:30 in Madrid
        assertEquals(LocalDate(2026, 12, 15), dayOf(instant, zone = TestZones.NEW_YORK)) // 23:30
        assertEquals(LocalDate(2026, 12, 16), dayOf(instant, zone = TimeZone.of("Asia/Tokyo"))) // 13:30
    }

    @Test
    fun `volando al oeste el dia se alarga y al este se acorta`() {
        val ny = TestZones.NEW_YORK
        val tokyo = TimeZone.of("Asia/Tokyo")
        val day = LocalDate(2026, 12, 16)

        // Day 16 starts at 04:00 in each zone: 03:00Z in Madrid, 09:00Z in NY, 19:00Z on the 15th in Tokyo.
        assertEquals(utc("2026-12-16T09:00:00Z"), LogicalDayCalculator.startOf(day, ny, four))
        assertEquals(utc("2026-12-15T19:00:00Z"), LogicalDayCalculator.startOf(day, tokyo, four))
        // You leave Madrid on the 16th (it began at 03:00Z) and land in NY: the 16th ends at 09:00Z on the 17th.
        val startInMadrid = LogicalDayCalculator.startOf(day, madrid, four)
        val endInNewYork = LogicalDayCalculator.endOf(day, ny, four)
        assertEquals(30.hours, endInNewYork - startInMadrid)
    }
}
