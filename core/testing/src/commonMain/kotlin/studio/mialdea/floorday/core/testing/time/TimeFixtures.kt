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
    /** Fecha base para minimumSince en los builders. */
    val BASELINE: LocalDate = LocalDate(2026, 11, 1)
    val SPRINT0_END: LocalDate = LocalDate(2026, 12, 6)

    /** Europe/Madrid salta de 02:00 a 03:00. */
    val DST_SPRING_2027: LocalDate = LocalDate(2027, 3, 28)

    /** Europe/Madrid repite de 02:00 a 03:00. */
    val DST_AUTUMN_2027: LocalDate = LocalDate(2027, 10, 31)
}

/**
 * Semántica de kotlinx-datetime: en un hueco DST usa el offset anterior
 * (02:30 del 28-mar-2027 = 01:30Z = 03:30 CEST); en un solape, el instante más temprano.
 */
fun instantOf(date: LocalDate, time: LocalTime, zone: TimeZone = TestZones.MADRID): Instant =
    LocalDateTime(date, time).toInstant(zone)
