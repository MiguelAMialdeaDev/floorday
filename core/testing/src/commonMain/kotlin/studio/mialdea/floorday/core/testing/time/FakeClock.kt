package studio.mialdea.floorday.core.testing.time

import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/** Manual clock. It does not advance on its own: use advanceBy, set or setLocal. */
class FakeClock(start: Instant = DEFAULT_START) : Clock {

    @Volatile
    private var current: Instant = start

    override fun now(): Instant = current

    fun set(instant: Instant) {
        current = instant
    }

    fun setLocal(date: LocalDate, time: LocalTime, zone: TimeZone = TestZones.MADRID) {
        current = instantOf(date, time, zone)
    }

    /** Forward only. To simulate the user setting the clock back, use set. */
    fun advanceBy(duration: Duration) {
        require(!duration.isNegative()) { "advanceBy necesita una duración positiva: $duration" }
        current += duration
    }

    companion object {
        /** 1-Dec-2026 at 08:00 in Madrid (UTC+1). */
        val DEFAULT_START: Instant = Instant.parse("2026-12-01T07:00:00Z")
    }
}
