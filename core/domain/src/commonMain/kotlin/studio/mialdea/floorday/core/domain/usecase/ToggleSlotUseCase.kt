package studio.mialdea.floorday.core.domain.usecase

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.EntrySource
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.SlotId
import studio.mialdea.floorday.core.domain.model.ToggleOutcome
import studio.mialdea.floorday.core.domain.repository.CompletionRepository

/**
 * One tap on a row. DONE becomes unmarked; unmarked or SKIPPED becomes DONE with the slot's
 * current amount (via [Habit.record], which validates the slot and the status).
 *
 * [previous] is the mark the caller is showing. The caller serializes taps, so it matches storage.
 */
class ToggleSlotUseCase(
    private val completions: CompletionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(habit: Habit, slotId: SlotId, day: LocalDate, previous: Completion?): ToggleOutcome {
        val current = if (previous?.status == CompletionStatus.DONE) {
            completions.remove(slotId, day)
            null
        } else {
            habit.record(slotId, day, CompletionStatus.DONE, at = clock.now(), source = EntrySource.APP)
                .also { completions.upsert(it) }
        }
        return ToggleOutcome(previous, current)
    }
}

/** Undo of a toggle: puts [previous] back exactly, or deletes the row if there was none. */
class RestoreCompletionUseCase(private val completions: CompletionRepository) {
    suspend operator fun invoke(slotId: SlotId, day: LocalDate, previous: Completion?) {
        if (previous == null) completions.remove(slotId, day) else completions.upsert(previous)
    }
}
