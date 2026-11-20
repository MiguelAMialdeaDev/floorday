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
 * Logical day: day D starts at the first instant at which the zone's wall clock reads
 * D + [DayStart] and ends just before D + 1 starts.
 *
 * Formal definition: `dayOf(i) = max { D | startOf(D) <= i }`. With it:
 * - Wall-clock time counts, not elapsed time: 03:59 is the previous day and 04:00 the new one,
 *   also on clock-change days (subtracting 4 h from the instant would give the wrong day).
 * - If the start time falls in the March gap (it does not exist), the day starts at the jump instant.
 * - If it falls in the October overlap (it exists twice), the day starts at the first occurrence and
 *   does not "go back" during the second one.
 * - It is monotonic: the logical day never goes backwards while the zone stays the same.
 *
 * Pure functions: the zone and the instant always arrive as parameters.
 */
object LogicalDayCalculator {

    fun dayOf(instant: Instant, timeZone: TimeZone, dayStart: DayStart): LocalDate {
        val local = instant.toLocalDateTime(timeZone)
        val candidate = if (local.time >= dayStart.time) local.date else local.date.minus(1, DateTimeUnit.DAY)
        // Only matters on the second pass of an overlap: the next day already started on the first.
        val next = candidate.plus(1, DateTimeUnit.DAY)
        return if (startOf(next, timeZone, dayStart) <= instant) next else candidate
    }

    /** First instant of the logical day [day] (inclusive). */
    fun startOf(day: LocalDate, timeZone: TimeZone, dayStart: DayStart): Instant {
        val wall = day.atTime(dayStart.time)
        // kotlinx-datetime: in a gap it uses the previous offset (lands after the jump) and in an overlap
        // it returns the earliest instant.
        val instant = wall.toInstant(timeZone)
        return if (instant.toLocalDateTime(timeZone) == wall) instant else gapTransition(wall, timeZone, instant)
    }

    /** Exclusive end of the logical day [day]: the start of the next one. */
    fun endOf(day: LocalDate, timeZone: TimeZone, dayStart: DayStart): Instant =
        startOf(day.plus(1, DateTimeUnit.DAY), timeZone, dayStart)

    /** Real length of the logical day: almost always 24 h, 23 or 25 h if it has a clock change. */
    fun lengthOf(day: LocalDate, timeZone: TimeZone, dayStart: DayStart): Duration =
        endOf(day, timeZone, dayStart) - startOf(day, timeZone, dayStart)

    /**
     * [wall] does not exist in the zone (DST gap). Finds the jump instant: the first one whose wall
     * clock is >= [wall]. [afterGap] is what `toInstant` returned, which is already after the jump.
     * Transitions in the tz database fall on whole seconds, so a binary search over seconds
     * gives the exact instant (about 12 iterations for a 1 h gap).
     */
    private fun gapTransition(wall: LocalDateTime, timeZone: TimeZone, afterGap: Instant): Instant {
        val offsetAfter = timeZone.offsetAt(afterGap)
        // The same wall time with the new offset lands before the jump.
        var low = wall.toInstant(offsetAfter).epochSeconds
        var high = afterGap.epochSeconds
        while (high - low > 1) {
            val mid = low + (high - low) / 2
            if (Instant.fromEpochSeconds(mid).toLocalDateTime(timeZone) >= wall) high = mid else low = mid
        }
        return Instant.fromEpochSeconds(high)
    }
}
