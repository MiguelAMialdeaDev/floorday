package studio.mialdea.floorday.core.domain.model

import kotlinx.datetime.LocalTime

/**
 * Un momento de un hábito: "Mañana 08:00, 1 pastilla". La hora es un LocalTime
 * "flotante" que sigue la zona del dispositivo, nunca un epoch. Una hora que no existe
 * un día de cambio de hora (02:30) es válida aquí: la resuelve ReminderPlanner.
 *
 * [label] puede estar vacío: la UI muestra entonces la hora o la franja.
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
