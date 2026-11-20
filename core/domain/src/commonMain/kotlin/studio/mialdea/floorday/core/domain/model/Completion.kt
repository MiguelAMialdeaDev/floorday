package studio.mialdea.floorday.core.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/**
 * Lo que pasó con un momento en un día lógico. Hay como máximo uno por (slotId, day):
 * esa es la PK en Room. Deshacer borra la fila. Se crea con [Habit.record], que
 * comprueba que el estado está permitido para el tipo de hábito; el constructor
 * queda público para los mappers de Room.
 */
data class Completion(
    val slotId: SlotId,
    /** Día lógico al que pertenece la marca; no se recalcula desde [recordedAt]. */
    val day: LocalDate,
    val status: CompletionStatus,
    val recordedAt: Instant,
    /** Copia de la cantidad del momento al registrar. */
    val amount: Quarters,
    val source: EntrySource,
) {
    val key: CompletionKey get() = CompletionKey(slotId, day)
}

data class CompletionKey(val slotId: SlotId, val day: LocalDate)
