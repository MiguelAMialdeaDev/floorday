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
import kotlinx.datetime.plus

/**
 * Zonas con cambios de hora a medianoche o de 30 min, con fechas ya pasadas (reglas fijas en la base tz).
 * America/Santiago 2024: el 7-abr a las 03:00Z las 24:00 del 6 (-03) vuelven a las 23:00 del 6 (-04);
 * el 8-sep a las 04:00Z las 00:00 (-04) saltan a las 01:00 (-03).
 * America/Havana 2024: 10-mar 00:00 salta a 01:00; 3-nov 01:00 vuelve a 00:00.
 * Australia/Lord_Howe 2024: 7-abr 02:00 vuelve a 01:30; 6-oct 02:00 salta a 02:30.
 */
class LogicalDayOtherZonesTest {

    private fun utc(iso: String) = Instant.parse(iso)

    @Test
    fun `Santiago con inicio a las 00_00 la hora repetida es del dia anterior`() {
        val santiago = TimeZone.of("America/Santiago")
        val midnight = DayStart.of(0)

        // A las 03:00Z del 7-abr-2024 el reloj pasa de 23:59:59 (-03) a 23:00 del 6 (-04): las 00:00 del 7
        // solo existen una vez, a las 04:00Z. El día 6 dura 25 h y la hora repetida sigue siendo el día 6.
        assertEquals(LocalDate(2024, 4, 6), LogicalDayCalculator.dayOf(utc("2024-04-07T03:30:00Z"), santiago, midnight))
        assertEquals(LocalDate(2024, 4, 7), LogicalDayCalculator.dayOf(utc("2024-04-07T04:00:00Z"), santiago, midnight))
        assertEquals(25.hours, LogicalDayCalculator.lengthOf(LocalDate(2024, 4, 6), santiago, midnight))
    }

    @Test
    fun `Santiago con inicio a las 00_00 en el hueco empieza a la 01_00`() {
        val santiago = TimeZone.of("America/Santiago")
        val midnight = DayStart.of(0)

        assertEquals(utc("2024-09-08T04:00:00Z"), LogicalDayCalculator.startOf(LocalDate(2024, 9, 8), santiago, midnight))
        assertEquals(23.hours, LogicalDayCalculator.lengthOf(LocalDate(2024, 9, 8), santiago, midnight))
    }

    @Test
    fun `barrido en zonas con cambios a medianoche y de media hora`() {
        val cases = listOf(
            "America/Santiago" to listOf("2024-04-05T00:00:00Z", "2024-09-06T00:00:00Z"),
            "America/Havana" to listOf("2024-03-08T00:00:00Z", "2024-11-01T00:00:00Z"),
            "Australia/Lord_Howe" to listOf("2024-04-05T00:00:00Z", "2024-10-04T00:00:00Z"),
        )
        val starts = listOf(
            DayStart.of(0),
            DayStart.of(1),
            DayStart.of(1, 45),
            DayStart.of(2),
            DayStart.of(2, 15),
            DayStart.Default,
        )
        for ((id, windows) in cases) {
            val zone = TimeZone.of(id)
            for (start in starts) for (from in windows) {
                var instant = utc(from)
                val until = instant + 96.hours
                var previous = LogicalDayCalculator.dayOf(instant, zone, start)
                while (instant < until) {
                    val day = LogicalDayCalculator.dayOf(instant, zone, start)
                    assertTrue(day == previous || day == previous.plus(1, DateTimeUnit.DAY), "salto en $instant $id $start")
                    assertTrue(LogicalDayCalculator.startOf(day, zone, start) <= instant, "startOf $instant $id $start")
                    assertTrue(instant < LogicalDayCalculator.endOf(day, zone, start), "endOf $instant $id $start")
                    previous = day
                    instant += 1.minutes
                }
            }
        }
    }
}
