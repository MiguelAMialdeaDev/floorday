package studio.mialdea.floorday.core.testing.builder

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.EntrySource
import studio.mialdea.floorday.core.domain.model.HabitSlot
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.testing.time.instantOf

private val NOON = LocalTime(12, 0)

/** Mark for a slot. It does not validate the habit kind: that is what Habit.record is for. */
fun aCompletion(
    slot: HabitSlot,
    day: LocalDate,
    status: CompletionStatus = CompletionStatus.DONE,
    recordedAt: Instant = instantOf(day, slot.time ?: NOON),
    amount: Quarters = slot.amount,
    source: EntrySource = EntrySource.APP,
): Completion = Completion(
    slotId = slot.id,
    day = day,
    status = status,
    recordedAt = recordedAt,
    amount = amount,
    source = source,
)
