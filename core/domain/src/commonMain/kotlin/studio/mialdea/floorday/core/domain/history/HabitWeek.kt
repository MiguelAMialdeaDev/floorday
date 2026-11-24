package studio.mialdea.floorday.core.domain.history

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.SlotId

/** What a habit got on one day of the week grid. */
enum class HabitDayMark {
    /** Every slot due that day was done. */
    DONE,

    /** A past day with some slot still not done. */
    MISSED,

    /** Today, not done yet: it can still be marked. */
    OPEN,

    /**
     * Nothing was due: weekday off, archived, created later, no slot that day or every dose skipped
     * on purpose. Neutral, never a failure.
     */
    REST,
}

data class HabitDay(val day: LocalDate, val mark: HabitDayMark)

/** One row of the grid: a habit and its marks, oldest day first. */
data class HabitWeekRow(val habit: Habit, val days: List<HabitDay>)

/** Last [days] logical days ending today, oldest first, one row per habit in the user order. */
data class HabitWeek(
    val today: LocalDate,
    val days: List<LocalDate>,
    val rows: List<HabitWeekRow>,
)

/**
 * Builds the week grid. Pure: today, the first day of each habit and the marks come from outside.
 * Every mark is per habit: a medication is DONE only when all its doses of that day are taken.
 */
object HabitWeekCalculator {

    const val DAY_COUNT: Int = 7

    fun compute(
        today: LocalDate,
        habits: List<Habit>,
        completions: List<Completion>,
        /** Logical day the habit was created on; earlier days are REST, not MISSED. */
        firstDayOf: (Habit) -> LocalDate,
        dayCount: Int = DAY_COUNT,
    ): HabitWeek {
        require(dayCount > 0) { "La cuadrícula necesita al menos un día" }
        val days = (dayCount - 1 downTo 0).map { today.minus(it, DateTimeUnit.DAY) }
        val byDay = completions.groupBy { it.day }.mapValues { (_, marks) -> marks.associateBy { it.slotId } }
        val rows = habits
            .filter { it.archivedOn == null || today < it.archivedOn }
            .map { habit ->
                val firstDay = firstDayOf(habit)
                HabitWeekRow(
                    habit = habit,
                    days = days.map { day ->
                        HabitDay(day, markOf(habit, day, today, firstDay, byDay[day].orEmpty()))
                    },
                )
            }
        return HabitWeek(today = today, days = days, rows = rows)
    }

    private fun markOf(
        habit: Habit,
        day: LocalDate,
        today: LocalDate,
        firstDay: LocalDate,
        marks: Map<SlotId, Completion>,
    ): HabitDayMark {
        if (day < firstDay || !habit.isScheduledOn(day)) return HabitDayMark.REST
        val due = habit.slotsOn(day).filterNot { slot ->
            habit.kind.allowsSkip && marks[slot.id]?.status?.excusesSlot == true
        }
        return when {
            due.isEmpty() -> HabitDayMark.REST
            due.all { marks[it.id]?.status?.countsAsDone == true } -> HabitDayMark.DONE
            day >= today -> HabitDayMark.OPEN
            else -> HabitDayMark.MISSED
        }
    }

    /** First and last day of the grid that ends on [today]. */
    fun rangeEndingOn(today: LocalDate, dayCount: Int = DAY_COUNT): ClosedRange<LocalDate> =
        today.minus(dayCount - 1, DateTimeUnit.DAY)..today
}
