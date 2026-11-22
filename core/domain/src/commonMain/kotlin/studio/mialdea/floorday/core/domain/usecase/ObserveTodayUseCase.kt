package studio.mialdea.floorday.core.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.minimum.MinimumDayEvaluator
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.TodayEntry
import studio.mialdea.floorday.core.domain.model.TodaySnapshot
import studio.mialdea.floorday.core.domain.repository.CompletionRepository
import studio.mialdea.floorday.core.domain.repository.HabitRepository
import studio.mialdea.floorday.core.domain.time.LogicalDayProvider

/** Today's habits and marks, re-queried when the logical day changes. */
class ObserveTodayUseCase(
    private val habits: HabitRepository,
    private val completions: CompletionRepository,
    private val logicalDay: LogicalDayProvider,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<TodaySnapshot> =
        logicalDay.observe().flatMapLatest { day ->
            // Archived ones too: a habit archived from tomorrow on is still due today.
            combine(habits.observeHabits(includeArchived = true), completions.observeDay(day)) { all, marks ->
                snapshot(day, all, marks)
            }
        }

    private fun snapshot(day: LocalDate, all: List<Habit>, marks: List<Completion>): TodaySnapshot {
        val minimum = MinimumDayEvaluator.evaluate(day, all, marks)
        val bySlot = marks.associateBy { it.slotId }
        val entries = all
            .filter { it.isScheduledOn(day) }
            .flatMap { habit ->
                habit.slotsOn(day).map { slot ->
                    TodayEntry(habit, slot, bySlot[slot.id], required = slot.id in minimum.requiredSlotIds)
                }
            }
        return TodaySnapshot(
            day = day,
            entries = entries,
            minimum = minimum,
            hasHabits = all.any { it.archivedOn == null || day < it.archivedOn },
        )
    }
}
