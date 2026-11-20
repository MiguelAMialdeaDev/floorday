package studio.mialdea.floorday.core.testing.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionKey
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.SlotId

/**
 * Estado compartido por los repos en memoria. Reproduce las FK CASCADE del esquema v1
 * (habit -> habit_slot -> completion) y la atomicidad de una transacción: cada escritura
 * es un único update, así que nadie observa un estado intermedio.
 * Ojo: a diferencia de Room, no se reemite si el contenido no cambia.
 */
class InMemoryFloordayStore(val strictForeignKeys: Boolean = true) {

    internal data class Tables(
        val habits: Map<HabitId, Habit> = emptyMap(),
        val completions: Map<CompletionKey, Completion> = emptyMap(),
    ) {
        fun slotExists(slotId: SlotId): Boolean = habits.values.any { habit -> habit.slot(slotId) != null }
    }

    internal val tables = MutableStateFlow(Tables())

    /** Si no es null, toda escritura lanza esta excepción (disco lleno o BD corrupta). */
    var failWith: Throwable? = null

    /** Si es true, la siguiente escritura falla una sola vez (para probar reintentos en la UI). */
    var failNextWrite: Boolean = false

    /** Escritura normal. La transformación debe ser pura: update puede reintentarla. */
    internal fun write(transform: (Tables) -> Tables) {
        failWith?.let { throw it }
        if (failNextWrite) {
            failNextWrite = false
            error("Escritura fallida a propósito (test)")
        }
        tables.update(transform)
    }

    /** Siembra del arrange de un test: ignora failWith y failNextWrite. */
    internal fun seed(transform: (Tables) -> Tables) {
        tables.update(transform)
    }

    fun clear() {
        tables.value = Tables()
    }
}
