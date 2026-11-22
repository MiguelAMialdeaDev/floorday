package studio.mialdea.floorday.core.domain.time

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDate

/** The current logical day as a stream, for screens that must roll over on their own (Today). */
interface LogicalDayProvider {
    fun observe(): Flow<LocalDate>

    /** Recompute now (ON_RESUME, zone change). Without observers it has no visible effect. */
    fun refresh()
}

/**
 * Emits the logical day and emits again when the day start is crossed. All the date maths lives
 * in [LogicalDayCalculator]; this class only decides when to look at the clock again.
 *
 * - It wakes up at least every [maxSleep] so a zone change is picked up without a refresh.
 * - `delay` runs on a monotonic clock that does not advance in deep sleep on Android, so the
 *   screen calls [refresh] on ON_RESUME.
 * - The refresh is a counter in a StateFlow, not a SharedFlow without replay: a refresh that
 *   arrives while the loop is emitting (nobody waiting at that moment) is not lost.
 */
class ClockLogicalDayProvider(
    private val clock: Clock,
    private val timeZones: TimeZoneProvider,
    private val dayStart: Flow<DayStart>,
    private val maxSleep: Duration = 15.minutes,
) : LogicalDayProvider {

    private val refreshTick = MutableStateFlow(0L)

    override fun refresh() {
        refreshTick.update { it + 1 }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<LocalDate> =
        dayStart
            .distinctUntilChanged()
            .flatMapLatest { start ->
                flow {
                    while (true) {
                        val seen = refreshTick.value
                        // Clock and zone are read once per pass so day and next start agree.
                        val now = clock.now()
                        val zone = timeZones.current()
                        val day = LogicalDayCalculator.dayOf(now, zone, start)
                        emit(day)
                        val untilNext = LogicalDayCalculator.endOf(day, zone, start) - now
                        withTimeoutOrNull(untilNext.coerceIn(1.milliseconds, maxSleep)) {
                            refreshTick.first { it != seen }
                        }
                    }
                }
            }
            .distinctUntilChanged()
}
