package studio.mialdea.floorday.core.domain.model

import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.minimum.MinimumDayEvaluation
import studio.mialdea.floorday.core.domain.minimum.MinimumDayEvaluator

/** One row of Today: a slot of a habit scheduled on the day, with its mark if it has one. */
data class TodayEntry(
    val habit: Habit,
    val slot: HabitSlot,
    val completion: Completion?,
    /** Part of the day's required set (a dose skipped on purpose is not). */
    val required: Boolean,
)

/** Read model of a logical day for Today. The screen maps it to its own UI state. */
data class TodaySnapshot(
    val day: LocalDate,
    /** Habits in the user order, then slots in the habit order. */
    val entries: List<TodayEntry>,
    /** X/N of the ring and "minimum met". Single source: [MinimumDayEvaluator]. */
    val minimum: MinimumDayEvaluation,
    /** Some habit is not archived, even if nothing is due today. Tells "empty" from "rest day". */
    val hasHabits: Boolean,
)

/** What a toggle left behind: [previous] is what undo puts back. */
data class ToggleOutcome(val previous: Completion?, val current: Completion?)
