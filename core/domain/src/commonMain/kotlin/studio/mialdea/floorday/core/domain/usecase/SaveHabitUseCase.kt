package studio.mialdea.floorday.core.domain.usecase

import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.HabitInput
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.HabitSlot
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.domain.model.SaveHabitResult
import studio.mialdea.floorday.core.domain.model.newHabitId
import studio.mialdea.floorday.core.domain.model.newSlotId
import studio.mialdea.floorday.core.domain.repository.HabitRepository
import studio.mialdea.floorday.core.domain.time.LogicalDayProvider

/**
 * Creates or updates a habit from the editor. The editor validates first; the [Habit]
 * constructor is the last guard, so an invalid input throws IllegalArgumentException.
 *
 * - A habit that enters the Minimum Day counts from today's logical day, never backwards; one
 *   that already counted keeps its date.
 * - A removed slot is ended on today (removedOn) and a slot added to an existing habit starts
 *   today (addedOn): past days keep the slots they had.
 */
class SaveHabitUseCase(
    private val habits: HabitRepository,
    private val clock: Clock,
    private val logicalDay: LogicalDayProvider,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(input: HabitInput): SaveHabitResult {
        val today = logicalDay.observe().first()
        val now = clock.now()
        val stored = input.id?.let { habits.getHabit(it) ?: return SaveHabitResult.NotFound }
        val id = stored?.id ?: ids.newHabitId()

        val habit = Habit(
            id = id,
            name = input.name.trim(),
            kind = input.kind,
            slots = slots(id, input, stored, today),
            days = input.days,
            minimumSince = when {
                !input.countsForMinimum -> null
                stored?.minimumSince != null -> stored.minimumSince
                else -> today
            },
            archivedOn = stored?.archivedOn,
            sortOrder = stored?.sortOrder ?: habits.nextSortOrder(),
            unitLabel = input.unitLabel?.trim()?.ifEmpty { null }?.takeIf { input.kind.allowsUnitLabel },
            createdAt = stored?.createdAt ?: now,
            updatedAt = now,
        )
        habits.save(habit)
        return SaveHabitResult.Saved(habit)
    }

    private fun slots(id: HabitId, input: HabitInput, stored: Habit?, today: LocalDate): List<HabitSlot> {
        val previous = stored?.slots.orEmpty().associateBy { it.id }
        val current = input.slots.map { slot ->
            val old = slot.id?.let { requireNotNull(previous[it]?.takeIf(HabitSlot::isCurrent)) { "Unknown slot $it" } }
            HabitSlot(
                id = old?.id ?: ids.newSlotId(),
                habitId = id,
                label = slot.label.trim(),
                time = slot.time,
                amount = slot.amount,
                reminder = slot.reminder,
                // An existing slot keeps its start (null = since the habit exists); a new one starts
                // today, except in a new habit, where it applies since the habit exists.
                addedOn = if (old != null) old.addedOn else today.takeIf { stored != null },
            )
        }
        val kept = current.map { it.id }.toSet()
        val ended = stored?.slots.orEmpty()
            .filterNot { it.id in kept }
            .map { if (it.isCurrent) it.copy(removedOn = today) else it }
            // Habit checks every slot against its kind, ended ones too. An ended slot is never
            // planned again and its records keep their own amount, so this rewrites no history.
            .map { it.fitTo(input.kind) }
        return current + ended
    }

    private fun HabitSlot.fitTo(kind: HabitKind): HabitSlot = copy(
        amount = if (kind.allowsAmount) amount else Quarters.ONE,
        reminder = if (reminder == ReminderMode.INSISTENT && !kind.allowsInsistentReminder) ReminderMode.NONE else reminder,
    )
}

/** Deletes the habit with its slots and its whole history. The editor asks for confirmation first. */
class DeleteHabitUseCase(private val habits: HabitRepository) {
    suspend operator fun invoke(id: HabitId) = habits.delete(id)
}
