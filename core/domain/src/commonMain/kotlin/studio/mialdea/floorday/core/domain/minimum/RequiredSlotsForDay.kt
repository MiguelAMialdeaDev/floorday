package studio.mialdea.floorday.core.domain.minimum

import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.SlotId

/**
 * Slots that must be completed on [day] for the Minimum Day to count as MET.
 *
 * - Habits with `countsForMinimumOn(day)` count: `minimumSince <= day`, the weekday is
 *   active and not archived that day (archiving is inclusive).
 * - Each slot is one required item: a 1 + ½ medication is two.
 * - A dose skipped on purpose (SKIPPED on a kind that allows it) leaves the required set: it is neutral.
 * - SKIPPED on a CHECK does not exist in the product; if it arrives, it is ignored and the slot stays pending.
 */
object RequiredSlotsForDay {

    data class Result(
        val required: Set<SlotId>,
        val skipped: Set<SlotId>,
    )

    fun compute(day: LocalDate, habits: List<Habit>, completions: List<Completion>): Result {
        val statusBySlot = completions.filter { it.day == day }.associate { it.slotId to it.status }
        val required = mutableSetOf<SlotId>()
        val skipped = mutableSetOf<SlotId>()
        habits.filter { it.countsForMinimumOn(day) }.forEach { habit ->
            habit.slots.forEach { slot ->
                val isNeutral = habit.kind.allowsSkip && statusBySlot[slot.id]?.excusesSlot == true
                if (isNeutral) skipped += slot.id else required += slot.id
            }
        }
        return Result(required = required, skipped = skipped)
    }
}
