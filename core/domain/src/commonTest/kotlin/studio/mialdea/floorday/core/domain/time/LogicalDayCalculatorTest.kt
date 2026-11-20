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
 * 28-mar-2027: a las 01:00Z el reloj salta de 02:00 a 03:00 (hueco 02:00-02:59).
 * 31-oct-2027: a las 01:00Z el reloj vuelve de 03:00 a 02:00 (solape 02:00-02:59).
 * Todos los instantes se escriben en UTC con la hora local al lado.
 */
class LogicalDayCalculatorTest {

    private val madrid = TestZones.MADRID
    private val four = DayStart.Default

    private fun utc(iso: String) = Instant.parse(iso)
    private fun dayOf(iso: String, start: DayStart = four, zone: TimeZone = madrid) =
        LogicalDayCalculator.dayOf(utc(iso), zone, start)

    // Medianoche y frontera de las 04:00

    @Test
    fun `medianoche local sigue siendo el dia anterior con inicio a las 04`() {
        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-15T23:00:00Z")) // 00:00 del 16
    }

    @Test
    fun `23_59 local es el mismo dia`() {
        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-15T22:59:00Z")) // 23:59 del 15
    }

    @Test
    fun `03_59_59 es el dia anterior y 04_00 el nuevo`() {
        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-16T02:59:00Z")) // 03:59
        assertEquals(LocalDate(2026, 12, 15), dayOf("2026-12-16T02:59:59.999Z")) // 03:59:59.999
        assertEquals(LocalDate(2026, 12, 16), dayOf("2026-12-16T03:00:00Z")) // 04:00
    }

    @Test
    fun `la media pastilla de las 23_30 confirmada a las 00_10 cuenta para su dia`() {
        val scheduled = dayOf("2026-12-15T22:30:00Z") // 23:30 del 15

        val confirmed = dayOf("2026-12-15T23:10:00Z") // 00:10 del 16

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

    // Cambio de hora de marzo (día de 23 h)

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
        assertEquals(LocalDate(2027, 3, 27), dayOf("2027-03-28T01:00:00Z")) // 03:00 CEST, tras el salto
        assertEquals(LocalDate(2027, 3, 27), dayOf("2027-03-28T01:59:59Z")) // 03:59:59 CEST
        assertEquals(LocalDate(2027, 3, 28), dayOf("2027-03-28T02:00:00Z")) // 04:00 CEST
    }

    @Test
    fun `marzo 2027 con inicio a las 00_00 el domingo 28 dura 23 h`() {
        assertEquals(23.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 3, 28), madrid, DayStart.of(0)))
    }

    @Test
    fun `restar 4 h al instante daria otro dia pero el reloj de pared no`() {
        // 04:30 CEST del 28 = 02:30Z. Restando 4 h de tiempo real: 22:30Z del 27 = 23:30 CET del 27 (mal).
        assertEquals(LocalDate(2027, 3, 28), dayOf("2027-03-28T02:30:00Z"))
    }

    // Hora 02:30 inexistente (hueco)

    @Test
    fun `inicio a las 02_30 en el hueco de marzo empieza en el instante del salto`() {
        val start = DayStart.of(2, 30)
        val day = LocalDate(2027, 3, 28)

        // toInstant devolvería 01:30Z (03:30 CEST) y perdería media hora del día.
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

    // Cambio de hora de octubre (día de 25 h)

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
        assertEquals(LocalDate(2027, 10, 31), dayOf("2027-10-31T01:15:00Z", start)) // 02:15 CET, segunda pasada
        assertEquals(LocalDate(2027, 10, 31), dayOf("2027-10-31T01:30:00Z", start)) // 02:30 CET
        assertEquals(25.hours, LogicalDayCalculator.lengthOf(LocalDate(2027, 10, 31), madrid, start))
    }

    // Propiedad: barrido minuto a minuto alrededor de los cambios

    @Test
    fun `barrido de los cambios de 2026 y 2027 el dia es monotono y coherente con startOf`() {
        val starts = listOf(0, 2, 3, 4, 6).map { DayStart.of(it) } + DayStart.of(2, 30) + DayStart.of(3, 59)
        val windows = listOf(
            "2026-03-27T00:00:00Z", // cambio del 29-mar-2026
            "2026-10-23T00:00:00Z", // cambio del 25-oct-2026
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

    // Cambio de zona en un viaje

    @Test
    fun `el mismo instante es otro dia logico en otra zona`() {
        val instant = "2026-12-16T04:30:00Z"

        assertEquals(LocalDate(2026, 12, 16), dayOf(instant, zone = madrid)) // 05:30 en Madrid
        assertEquals(LocalDate(2026, 12, 15), dayOf(instant, zone = TestZones.NEW_YORK)) // 23:30
        assertEquals(LocalDate(2026, 12, 16), dayOf(instant, zone = TimeZone.of("Asia/Tokyo"))) // 13:30
    }

    @Test
    fun `volando al oeste el dia se alarga y al este se acorta`() {
        val ny = TestZones.NEW_YORK
        val tokyo = TimeZone.of("Asia/Tokyo")
        val day = LocalDate(2026, 12, 16)

        // El día 16 empieza a las 04:00 de cada zona: 03:00Z en Madrid, 09:00Z en NY, 19:00Z del 15 en Tokio.
        assertEquals(utc("2026-12-16T09:00:00Z"), LogicalDayCalculator.startOf(day, ny, four))
        assertEquals(utc("2026-12-15T19:00:00Z"), LogicalDayCalculator.startOf(day, tokyo, four))
        // Sales de Madrid el 16 (empezó a las 03:00Z) y aterrizas en NY: el 16 acaba a las 09:00Z del 17.
        val startInMadrid = LogicalDayCalculator.startOf(day, madrid, four)
        val endInNewYork = LogicalDayCalculator.endOf(day, ny, four)
        assertEquals(30.hours, endInNewYork - startInMadrid)
    }
}
