package studio.mialdea.floorday.core.domain.time

import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.offsetAt
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Día lógico: el día D empieza en el primer instante en que el reloj de pared de la zona marca
 * D + [DayStart] y termina justo antes de que empiece D + 1.
 *
 * Definición formal: `dayOf(i) = máx { D | startOf(D) <= i }`. Con ella:
 * - Cuenta el reloj de pared, no el tiempo transcurrido: 03:59 es el día anterior y 04:00 el nuevo,
 *   también los días de cambio de hora (restar 4 h al instante daría el día equivocado).
 * - Si la hora de inicio cae en el hueco de marzo (no existe), el día empieza en el instante del salto.
 * - Si cae en el solape de octubre (existe dos veces), el día empieza en la primera ocurrencia y
 *   no "vuelve atrás" durante la segunda.
 * - Es monótona: el día lógico nunca retrocede mientras no cambie la zona.
 *
 * Funciones puras: la zona y el instante siempre llegan como parámetro.
 */
object LogicalDayCalculator {

    fun dayOf(instant: Instant, timeZone: TimeZone, dayStart: DayStart): LocalDate {
        val local = instant.toLocalDateTime(timeZone)
        val candidate = if (local.time >= dayStart.time) local.date else local.date.minus(1, DateTimeUnit.DAY)
        // Solo cambia algo en la segunda pasada de un solape: el día siguiente ya empezó en la primera.
        val next = candidate.plus(1, DateTimeUnit.DAY)
        return if (startOf(next, timeZone, dayStart) <= instant) next else candidate
    }

    /** Primer instante del día lógico [day] (inclusivo). */
    fun startOf(day: LocalDate, timeZone: TimeZone, dayStart: DayStart): Instant {
        val wall = day.atTime(dayStart.time)
        // kotlinx-datetime: en un hueco usa el offset anterior (cae después del salto) y en un solape
        // devuelve el instante más temprano.
        val instant = wall.toInstant(timeZone)
        return if (instant.toLocalDateTime(timeZone) == wall) instant else gapTransition(wall, timeZone, instant)
    }

    /** Fin exclusivo del día lógico [day]: el inicio del siguiente. */
    fun endOf(day: LocalDate, timeZone: TimeZone, dayStart: DayStart): Instant =
        startOf(day.plus(1, DateTimeUnit.DAY), timeZone, dayStart)

    /** Duración real del día lógico: 24 h casi siempre, 23 o 25 h si contiene un cambio de hora. */
    fun lengthOf(day: LocalDate, timeZone: TimeZone, dayStart: DayStart): Duration =
        endOf(day, timeZone, dayStart) - startOf(day, timeZone, dayStart)

    /**
     * [wall] no existe en la zona (hueco de DST). Busca el instante del salto: el primero cuyo reloj
     * de pared es >= [wall]. [afterGap] es lo que devolvió `toInstant`, que ya está después del salto.
     * Los saltos de la base tz caen en segundos exactos, así que una búsqueda binaria por segundos
     * da el instante exacto (unas 12 iteraciones para un hueco de 1 h).
     */
    private fun gapTransition(wall: LocalDateTime, timeZone: TimeZone, afterGap: Instant): Instant {
        val offsetAfter = timeZone.offsetAt(afterGap)
        // La misma hora de pared con el offset nuevo cae antes del salto.
        var low = wall.toInstant(offsetAfter).epochSeconds
        var high = afterGap.epochSeconds
        while (high - low > 1) {
            val mid = low + (high - low) / 2
            if (Instant.fromEpochSeconds(mid).toLocalDateTime(timeZone) >= wall) high = mid else low = mid
        }
        return Instant.fromEpochSeconds(high)
    }
}
