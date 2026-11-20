package studio.mialdea.floorday.core.domain.minimum

import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.SlotId

/**
 * Provisional state of a day (open or not). It does not replace DayStatus: when the day closes (S4),
 * NOT_STARTED and PARTIAL become MISSED or FROZEN; MET and REST are kept. Nothing is spent here.
 */
enum class MinimumDayOutcome {
    /** All required items done. */
    MET,

    /** Some done, not all. */
    PARTIAL,

    /** None done yet (UI only; it does not exist in DayStatus). */
    NOT_STARTED,

    /** Nothing required that day (nothing active or every dose skipped on purpose): neutral. */
    REST,
}

data class MinimumDayEvaluation(
    val day: LocalDate,
    val requiredSlotIds: Set<SlotId>,
    val doneSlotIds: Set<SlotId>,
    val skippedSlotIds: Set<SlotId>,
) {
    /** N of the X/N ring. */
    val required: Int get() = requiredSlotIds.size

    /** X of the X/N ring. */
    val done: Int get() = doneSlotIds.size

    val outcome: MinimumDayOutcome
        get() = when {
            required == 0 -> MinimumDayOutcome.REST
            done == required -> MinimumDayOutcome.MET
            done == 0 -> MinimumDayOutcome.NOT_STARTED
            else -> MinimumDayOutcome.PARTIAL
        }

    val isMet: Boolean get() = outcome == MinimumDayOutcome.MET
}

/**
 * Evaluates the Minimum Day of a logical day. Pure: it knows neither the time nor your zone.
 * It is the single source of the X/N ring and of "minimum met" in Today.
 */
object MinimumDayEvaluator {

    fun evaluate(day: LocalDate, habits: List<Habit>, completions: List<Completion>): MinimumDayEvaluation {
        val slots = RequiredSlotsForDay.compute(day, habits, completions)
        val doneToday = completions
            .filter { it.day == day && it.status.countsAsDone }
            .mapTo(mutableSetOf()) { it.slotId }
        return MinimumDayEvaluation(
            day = day,
            requiredSlotIds = slots.required,
            doneSlotIds = slots.required intersect doneToday,
            skippedSlotIds = slots.skipped,
        )
    }
}

enum class MinimumSizeCheck { TOO_FEW, OK, TOO_MANY }

/**
 * The minimum has 3 to 5 items. An item is a habit, not a slot: a medication
 * with two doses counts as 1. It is a soft limit shown by onboarding and "My minimum"; the evaluator
 * works the same with 2 or with 6.
 */
object MinimumSize {
    const val MIN_ITEMS: Int = 3
    const val MAX_ITEMS: Int = Habit.SOFT_MINIMUM_LIMIT

    fun check(habits: List<Habit>, on: LocalDate): MinimumSizeCheck {
        val count = habits.count { it.countsForMinimum && (it.archivedOn == null || on < it.archivedOn) }
        return when {
            count < MIN_ITEMS -> MinimumSizeCheck.TOO_FEW
            count > MAX_ITEMS -> MinimumSizeCheck.TOO_MANY
            else -> MinimumSizeCheck.OK
        }
    }
}
