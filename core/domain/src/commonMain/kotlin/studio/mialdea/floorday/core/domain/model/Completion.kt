package studio.mialdea.floorday.core.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/**
 * What happened to a slot on a logical day. There is at most one per (slotId, day):
 * that is the PK in Room. Undo deletes the row. It is created with [Habit.record], which
 * checks that the status is allowed for the habit kind; the constructor
 * stays public for the Room mappers.
 */
data class Completion(
    val slotId: SlotId,
    /** Logical day the mark belongs to; it is not recomputed from [recordedAt]. */
    val day: LocalDate,
    val status: CompletionStatus,
    val recordedAt: Instant,
    /** Copy of the slot's amount at recording time. */
    val amount: Quarters,
    val source: EntrySource,
) {
    val key: CompletionKey get() = CompletionKey(slotId, day)
}

data class CompletionKey(val slotId: SlotId, val day: LocalDate)
