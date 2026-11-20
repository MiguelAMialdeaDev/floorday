package studio.mialdea.floorday.core.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/**
 * Un hábito del usuario. La medicación es un hábito con kind = MEDICATION, varios
 * momentos y cantidad en cuartos. El orden de [slots] es el que ve el usuario y se
 * persiste (columna position).
 *
 * Los timestamps los pone el caso de uso con el Clock inyectado; el modelo nunca lee
 * la hora del sistema.
 */
data class Habit(
    val id: HabitId,
    val name: String,
    val kind: HabitKind,
    val slots: List<HabitSlot>,
    val days: DaysOfWeek,
    /** Día lógico desde el que cuenta para el Día Mínimo; null si no cuenta. */
    val minimumSince: LocalDate?,
    /** Día lógico en que se archivó; ese día ya no se programa. */
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
        require(slots.isNotEmpty()) { "Un hábito necesita al menos un momento" }
        require(slots.size <= MAX_SLOTS) { "Un hábito admite $MAX_SLOTS momentos como máximo" }
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

    /** Derivado para que no exista el estado "cuenta pero sin fecha" (ni al revés). */
    val countsForMinimum: Boolean get() = minimumSince != null

    val isArchived: Boolean get() = archivedOn != null

    val isMedication: Boolean get() = kind == HabitKind.MEDICATION

    fun slot(id: SlotId): HabitSlot? = slots.firstOrNull { it.id == id }

    /** El hábito toca ese día lógico: día de la semana activo y no archivado todavía. */
    fun isScheduledOn(day: LocalDate): Boolean =
        day.dayOfWeek in days && (archivedOn == null || day < archivedOn)

    /**
     * Cuenta para el Día Mínimo ese día lógico. No mira las omisiones a propósito:
     * eso lo hace el evaluador del Día Mínimo con las Completion del día.
     */
    fun countsForMinimumOn(day: LocalDate): Boolean {
        val since = minimumSince ?: return false
        return since <= day && isScheduledOn(day)
    }

    fun canRecord(status: CompletionStatus): Boolean =
        status != CompletionStatus.SKIPPED || kind.allowsSkip

    /**
     * Crea el registro de un momento copiando su cantidad actual, para que editar la
     * cantidad después no reescriba el historial. No comprueba si el día está programado:
     * una acción de notificación puede llegar tarde y debe poder registrarse.
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

        /** Límite suave de "Mi mínimo" (se explica en la UI, no se impone aquí). */
        const val SOFT_MINIMUM_LIMIT = 5
    }
}
