package studio.mialdea.floorday.core.domain.minimum

import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.SlotId

/**
 * Momentos que hay que cumplir en [day] para que el Día Mínimo cuente como MET.
 *
 * - Cuentan los hábitos con `countsForMinimumOn(day)`: `minimumSince <= day`, día de la semana
 *   activo y sin archivar ese día (archivar es inclusivo).
 * - Cada momento es un requerido: una medicación de 1 + ½ son dos.
 * - Una toma omitida a propósito (SKIPPED en un tipo que lo admite) sale de los requeridos: es neutra.
 * - SKIPPED en un CHECK no existe en el producto; si llega, se ignora y el momento sigue pendiente.
 */
object RequiredSlotsForDay {

    data class Result(
        val required: Set<SlotId>,
        val skipped: Set<SlotId>,
    )

    fun compute(day: LocalDate, habits: List<Habit>, completions: List<Completion>): Result {
        val statusBySlot = completions.filter { it.day == day }.associate { it.slotId to it.status }
        val required = mutableSetOf<SlotId>()
        val skipped = mutableSetOf<SlotId>()
        habits.filter { it.countsForMinimumOn(day) }.forEach { habit ->
            habit.slots.forEach { slot ->
                val isNeutral = habit.kind.allowsSkip && statusBySlot[slot.id]?.excusesSlot == true
                if (isNeutral) skipped += slot.id else required += slot.id
            }
        }
        return Result(required = required, skipped = skipped)
    }
}
