package studio.mialdea.floorday.core.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import studio.mialdea.floorday.core.domain.history.HabitWeek
import studio.mialdea.floorday.core.domain.history.HabitWeekCalculator
import studio.mialdea.floorday.core.domain.repository.CompletionRepository
import studio.mialdea.floorday.core.domain.repository.HabitRepository
import studio.mialdea.floorday.core.domain.time.DayStart
import studio.mialdea.floorday.core.domain.time.LogicalDayCalculator
import studio.mialdea.floorday.core.domain.time.LogicalDayProvider
import studio.mialdea.floorday.core.domain.time.TimeZoneProvider

/** The last 7 logical days of every habit, re-queried when the logical day changes. */
class ObserveHabitWeekUseCase(
    private val habits: HabitRepository,
    private val completions: CompletionRepository,
    private val logicalDay: LogicalDayProvider,
    private val timeZones: TimeZoneProvider,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<HabitWeek> =
        logicalDay.observe().flatMapLatest { today ->
            val range = HabitWeekCalculator.rangeEndingOn(today)
            // Archived ones too: one archived from tomorrow on still has this week.
            combine(
                habits.observeHabits(includeArchived = true),
                completions.observeRange(range.start, range.endInclusive),
            ) { all, marks ->
                // DayStart stays fixed until the setting arrives (S1), as in Today.
                val zone = timeZones.current()
                HabitWeekCalculator.compute(today, all, marks, firstDayOf = { habit ->
                    LogicalDayCalculator.dayOf(habit.createdAt, zone, DayStart.Default)
                })
            }
        }
}
