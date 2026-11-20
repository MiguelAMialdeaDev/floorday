package studio.mialdea.floorday.core.domain.time

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/**
 * Entry point with injected dependencies (Clock and zone) for ViewModels and use cases.
 * The logic lives in [LogicalDayCalculator]; this class only wires clock, zone and setting.
 * The zone is requested once per call and never stored.
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
     * Instant at which the next logical day starts: `endOf(dayOf(now))`. It is what the Today
     * ticker waits for to re-emit the day. It reads the clock and the zone only once.
     */
    fun nextStart(dayStart: DayStart): Instant {
        val zone = timeZoneProvider.current()
        val today = LogicalDayCalculator.dayOf(clock.now(), zone, dayStart)
        return LogicalDayCalculator.endOf(today, zone, dayStart)
    }
}
