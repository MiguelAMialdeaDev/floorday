package studio.mialdea.floorday.core.testing.time

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

object TestZones {
    val MADRID: TimeZone = TimeZone.of("Europe/Madrid")
    val NEW_YORK: TimeZone = TimeZone.of("America/New_York")
    val UTC: TimeZone = TimeZone.UTC
}

object TestDates {
    /** Base date for minimumSince in the builders. */
    val BASELINE: LocalDate = LocalDate(2026, 11, 1)
    val SPRINT0_END: LocalDate = LocalDate(2026, 12, 6)

    /** Europe/Madrid jumps from 02:00 to 03:00. */
    val DST_SPRING_2027: LocalDate = LocalDate(2027, 3, 28)

    /** Europe/Madrid repeats 02:00 to 03:00. */
    val DST_AUTUMN_2027: LocalDate = LocalDate(2027, 10, 31)
}

/**
 * kotlinx-datetime semantics: in a DST gap it uses the previous offset
 * (02:30 on 28-Mar-2027 = 01:30Z = 03:30 CEST); in an overlap, the earliest instant.
 */
fun instantOf(date: LocalDate, time: LocalTime, zone: TimeZone = TestZones.MADRID): Instant =
    LocalDateTime(date, time).toInstant(zone)
