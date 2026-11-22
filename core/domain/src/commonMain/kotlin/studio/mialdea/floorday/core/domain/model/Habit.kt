package studio.mialdea.floorday.core.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/**
 * A user habit. Medication is a habit with kind = MEDICATION, several
 * slots and an amount in quarters. The order of [slots] is the one the user sees and it is
 * persisted (position column). It holds every slot ever, removed ones included so they keep
 * their history; [currentSlots] are the ones still in use.
 *
 * Timestamps are set by the use case with the injected Clock; the model never reads
 * the system time.
 */
data class Habit(
    val id: HabitId,
    val name: String,
    val kind: HabitKind,
    val slots: List<HabitSlot>,
    val days: DaysOfWeek,
    /** Logical day from which it counts for the Minimum Day; null if it does not count. */
    val minimumSince: LocalDate?,
    /** Logical day on which it was archived; it is no longer scheduled from that day. */
    val archivedOn: LocalDate?,
    val sortOrder: Int,
    val unitLabel: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(name.isNotBlank()) { "El nombre del hábito no puede estar vacío" }
        require(name == name.trim()) { "El nombre del hábito no puede empezar ni acabar en espacio" }
        require(name.length <= NAME_MAX_LENGTH) {
            "El nombre del hábito admite $NAME_MAX_LENGTH caracteres como máximo"
        }
        require(currentSlots.isNotEmpty()) { "Un hábito necesita al menos un momento vigente" }
        require(currentSlots.size <= MAX_SLOTS) { "Un hábito admite $MAX_SLOTS momentos vigentes como máximo" }
        require(slots.map { it.id }.toSet().size == slots.size) { "Hay momentos con el id repetido" }
        require(slots.all { it.habitId == id }) { "Todos los momentos deben pertenecer al hábito $id" }
        require(!days.isEmpty) { "Un hábito necesita al menos un día de la semana" }
        require(sortOrder >= 0) { "sortOrder no puede ser negativo: $sortOrder" }
        require(updatedAt >= createdAt) { "updatedAt ($updatedAt) es anterior a createdAt ($createdAt)" }

        if (!kind.allowsAmount) {
            require(slots.all { it.amount == Quarters.ONE }) { "Un hábito $kind no admite cantidades" }
        }
        if (!kind.allowsInsistentReminder) {
            require(slots.none { it.reminder == ReminderMode.INSISTENT }) {
                "Un hábito $kind no admite recordatorio insistente"
            }
        }
        if (unitLabel != null) {
            require(kind.allowsUnitLabel) { "Un hábito $kind no admite unidad" }
            require(unitLabel.isNotBlank() && unitLabel == unitLabel.trim()) {
                "La unidad no puede estar vacía ni empezar o acabar en espacio"
            }
            require(unitLabel.length <= UNIT_LABEL_MAX_LENGTH) {
                "La unidad admite $UNIT_LABEL_MAX_LENGTH caracteres como máximo"
            }
        }
    }

    /** Derived so the state "counts but has no date" (or the reverse) cannot exist. */
    val countsForMinimum: Boolean get() = minimumSince != null

    val isArchived: Boolean get() = archivedOn != null

    val isMedication: Boolean get() = kind == HabitKind.MEDICATION

    fun slot(id: SlotId): HabitSlot? = slots.firstOrNull { it.id == id }

    /** Slots not removed, in the user order. */
    val currentSlots: List<HabitSlot> get() = slots.filter { it.isCurrent }

    /** Slots that existed on that logical day, so a later edit never rewrites a past day. */
    fun slotsOn(day: LocalDate): List<HabitSlot> = slots.filter { it.isActiveOn(day) }

    /** The habit is due on that logical day: active weekday and not archived yet. */
    fun isScheduledOn(day: LocalDate): Boolean =
        day.dayOfWeek in days && (archivedOn == null || day < archivedOn)

    /**
     * Counts for the Minimum Day on that logical day. It ignores on-purpose skips:
     * the Minimum Day evaluator handles those with the day's Completions.
     */
    fun countsForMinimumOn(day: LocalDate): Boolean {
        val since = minimumSince ?: return false
        return since <= day && isScheduledOn(day)
    }

    fun canRecord(status: CompletionStatus): Boolean =
        status != CompletionStatus.SKIPPED || kind.allowsSkip

    /**
     * Creates the record of a slot by copying its current amount, so that editing the
     * amount later does not rewrite history. It does not check whether the day is scheduled:
     * a notification action can arrive late and must still be recordable.
     */
    fun record(
        slotId: SlotId,
        day: LocalDate,
        status: CompletionStatus,
        at: Instant,
        source: EntrySource,
    ): Completion {
        val slot = requireNotNull(slot(slotId)) { "El momento $slotId no pertenece al hábito $id" }
        require(canRecord(status)) { "Un hábito $kind no admite el estado $status" }
        return Completion(
            slotId = slot.id,
            day = day,
            status = status,
            recordedAt = at,
            amount = slot.amount,
            source = source,
        )
    }

    companion object {
        const val NAME_MAX_LENGTH = 40
        const val UNIT_LABEL_MAX_LENGTH = 16
        const val MAX_SLOTS = 6

        /** Soft limit of "My minimum" (explained in the UI, not enforced here). */
        const val SOFT_MINIMUM_LIMIT = 5
    }
}
