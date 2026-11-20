package studio.mialdea.floorday.core.domain.time

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/**
 * Punto de entrada con dependencias inyectadas (Clock y zona) para ViewModels y casos de uso.
 * La lógica vive en [LogicalDayCalculator]; esta clase solo une reloj, zona y ajuste.
 * La zona se pide una vez por llamada, nunca se guarda.
 */
class LogicalDayResolver(
    private val clock: Clock,
    private val timeZoneProvider: TimeZoneProvider,
) {
    fun today(dayStart: DayStart): LocalDate = dayOf(clock.now(), dayStart)

    fun dayOf(instant: Instant, dayStart: DayStart): LocalDate =
        LogicalDayCalculator.dayOf(instant, timeZoneProvider.current(), dayStart)

    fun startOf(day: LocalDate, dayStart: DayStart): Instant =
        LogicalDayCalculator.startOf(day, timeZoneProvider.current(), dayStart)

    fun endOf(day: LocalDate, dayStart: DayStart): Instant =
        LogicalDayCalculator.endOf(day, timeZoneProvider.current(), dayStart)

    /**
     * Instante en que empieza el próximo día lógico: `endOf(dayOf(now))`. Es lo que espera el
     * ticker de Hoy para reemitir el día. Usa una sola lectura de reloj y de zona.
     */
    fun nextStart(dayStart: DayStart): Instant {
        val zone = timeZoneProvider.current()
        val today = LogicalDayCalculator.dayOf(clock.now(), zone, dayStart)
        return LogicalDayCalculator.endOf(today, zone, dayStart)
    }
}
