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
 * Zones with clock changes at midnight or of 30 min, on past dates (rules fixed in the tz database).
 * America/Santiago 2024: on 7-Apr at 03:00Z, 24:00 on the 6th (-03) goes back to 23:00 on the 6th (-04);
 * on 8-Sep at 04:00Z, 00:00 (-04) jumps to 01:00 (-03).
 * America/Havana 2024: 10-Mar 00:00 jumps to 01:00; 3-Nov 01:00 goes back to 00:00.
 * Australia/Lord_Howe 2024: 7-Apr 02:00 goes back to 01:30; 6-Oct 02:00 jumps to 02:30.
 */
class LogicalDayOtherZonesTest {

    private fun utc(iso: String) = Instant.parse(iso)

    @Test
    fun `Santiago con inicio a las 00_00 la hora repetida es del dia anterior`() {
        val santiago = TimeZone.of("America/Santiago")
        val midnight = DayStart.of(0)

        // At 03:00Z on 7-Apr-2024 the clock goes from 23:59:59 (-03) to 23:00 on the 6th (-04): 00:00 on the 7th
        // exists only once, at 04:00Z. Day 6 lasts 25 h and the repeated hour is still day 6.
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
