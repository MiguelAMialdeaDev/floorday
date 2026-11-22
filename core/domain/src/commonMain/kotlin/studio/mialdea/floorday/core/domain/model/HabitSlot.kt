package studio.mialdea.floorday.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * A slot of a habit: "Morning 08:00, 1 pill". The time is a "floating" LocalTime
 * that follows the device zone, never an epoch. A time that does not exist
 * on a clock-change day (02:30) is valid here: ReminderPlanner resolves it.
 *
 * [label] can be empty: the UI then shows the time or the time band.
 *
 * A slot with history is never deleted: it is ended with [removedOn], so past days keep the
 * slots they had and their records. [addedOn] does the same for a slot added later.
 */
data class HabitSlot(
    val id: SlotId,
    val habitId: HabitId,
    val label: String,
    val time: LocalTime?,
    val amount: Quarters,
    val reminder: ReminderMode,
    /** First logical day the slot applies; null = since the habit exists. */
    val addedOn: LocalDate? = null,
    /** First logical day the slot no longer applies (exclusive, like Habit.archivedOn); null = current. */
    val removedOn: LocalDate? = null,
) {
    init {
        require(label == label.trim()) { "La etiqueta del momento no puede empezar ni acabar en espacio" }
        require(label.length <= LABEL_MAX_LENGTH) {
            "La etiqueta del momento admite $LABEL_MAX_LENGTH caracteres como máximo"
        }
        require(time == null || (time.second == 0 && time.nanosecond == 0)) {
            "La hora del momento va en minutos exactos (se guarda como minuto del día): $time"
        }
        require(!reminder.requiresTime || time != null) {
            "El recordatorio $reminder necesita una hora"
        }
        require(addedOn == null || removedOn == null || addedOn <= removedOn) {
            "El momento no puede retirarse ($removedOn) antes de añadirse ($addedOn)"
        }
    }

    /** Still part of the habit: not removed. */
    val isCurrent: Boolean get() = removedOn == null

    /** The slot existed on that logical day: added on or before it and not removed yet. */
    fun isActiveOn(day: LocalDate): Boolean =
        (addedOn == null || addedOn <= day) && (removedOn == null || day < removedOn)

    companion object {
        const val LABEL_MAX_LENGTH = 24
    }
}
