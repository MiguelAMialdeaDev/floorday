package studio.mialdea.floorday.core.domain.time

import kotlin.jvm.JvmInline
import kotlinx.datetime.LocalTime

/**
 * Wall-clock time at which the logical day starts (04:00 by default).
 *
 * Stored as minutes since midnight, same as `dayStartMinutes` in the settings.
 * The allowed range is 00:00 to 06:00: anything later stops being "the night before" and the product
 * does not need it. Widening it only changes [MAX_MINUTES]; the algorithm works with any time.
 */
@JvmInline
value class DayStart private constructor(val minutesOfDay: Int) {

    val time: LocalTime
        get() = LocalTime(hour = minutesOfDay / MINUTES_PER_HOUR, minute = minutesOfDay % MINUTES_PER_HOUR)

    override fun toString(): String = time.toString()

    companion object {
        private const val MINUTES_PER_HOUR = 60
        const val MIN_MINUTES: Int = 0
        const val MAX_MINUTES: Int = 6 * MINUTES_PER_HOUR
        const val DEFAULT_MINUTES: Int = 4 * MINUTES_PER_HOUR

        val Default: DayStart = DayStart(DEFAULT_MINUTES)

        fun ofMinutes(minutesOfDay: Int): DayStart {
            require(minutesOfDay in MIN_MINUTES..MAX_MINUTES) {
                "El inicio del día debe estar entre 00:00 y 06:00, y es $minutesOfDay min"
            }
            return DayStart(minutesOfDay)
        }

        fun of(hour: Int, minute: Int = 0): DayStart {
            require(minute in 0 until MINUTES_PER_HOUR) { "Minuto fuera de rango: $minute" }
            return ofMinutes(hour * MINUTES_PER_HOUR + minute)
        }

        /** For reading stored data: a corrupt or out-of-range value falls back to 04:00 instead of failing. */
        fun fromStoredOrDefault(minutesOfDay: Int?): DayStart =
            if (minutesOfDay != null && minutesOfDay in MIN_MINUTES..MAX_MINUTES) DayStart(minutesOfDay) else Default
    }
}
