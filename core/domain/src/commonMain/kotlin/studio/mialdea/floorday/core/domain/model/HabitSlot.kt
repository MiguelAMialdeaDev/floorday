package studio.mialdea.floorday.core.domain.model

import kotlinx.datetime.LocalTime

/**
 * A slot of a habit: "Morning 08:00, 1 pill". The time is a "floating" LocalTime
 * that follows the device zone, never an epoch. A time that does not exist
 * on a clock-change day (02:30) is valid here: ReminderPlanner resolves it.
 *
 * [label] can be empty: the UI then shows the time or the time band.
 */
data class HabitSlot(
    val id: SlotId,
    val habitId: HabitId,
    val label: String,
    val time: LocalTime?,
    val amount: Quarters,
    val reminder: ReminderMode,
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
    }

    companion object {
        const val LABEL_MAX_LENGTH = 24
    }
}
