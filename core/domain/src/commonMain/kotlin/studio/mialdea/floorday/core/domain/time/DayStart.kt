package studio.mialdea.floorday.core.domain.time

import kotlin.jvm.JvmInline
import kotlinx.datetime.LocalTime

/**
 * Hora de reloj a la que empieza el día lógico (04:00 por defecto).
 *
 * Se guarda en minutos desde medianoche, igual que `dayStartMinutes` en los ajustes.
 * El rango admitido es de 00:00 a 06:00: más tarde deja de ser "la noche anterior" y el producto
 * no lo necesita. Ampliarlo solo cambia [MAX_MINUTES], el algoritmo funciona con cualquier hora.
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

        /** Para leer lo guardado: un valor corrupto o fuera de rango vuelve a 04:00 en vez de romper. */
        fun fromStoredOrDefault(minutesOfDay: Int?): DayStart =
            if (minutesOfDay != null && minutesOfDay in MIN_MINUTES..MAX_MINUTES) DayStart(minutesOfDay) else Default
    }
}
