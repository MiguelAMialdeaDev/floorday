package studio.mialdea.floorday.core.domain.minimum

import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.SlotId

/**
 * Estado provisional de un día (abierto o no). No sustituye a DayStatus: al cerrar el día (S4),
 * NOT_STARTED y PARTIAL pasan a MISSED o FROZEN; MET y REST se conservan. Aquí no se gasta nada.
 */
enum class MinimumDayOutcome {
    /** Todos los requeridos hechos. */
    MET,

    /** Algunos hechos, no todos. */
    PARTIAL,

    /** Ninguno hecho todavía (solo para la UI; no existe en DayStatus). */
    NOT_STARTED,

    /** No hay requeridos ese día (nada activo o todas las tomas omitidas a propósito): neutro. */
    REST,
}

data class MinimumDayEvaluation(
    val day: LocalDate,
    val requiredSlotIds: Set<SlotId>,
    val doneSlotIds: Set<SlotId>,
    val skippedSlotIds: Set<SlotId>,
) {
    /** N del anillo X/N. */
    val required: Int get() = requiredSlotIds.size

    /** X del anillo X/N. */
    val done: Int get() = doneSlotIds.size

    val outcome: MinimumDayOutcome
        get() = when {
            required == 0 -> MinimumDayOutcome.REST
            done == required -> MinimumDayOutcome.MET
            done == 0 -> MinimumDayOutcome.NOT_STARTED
            else -> MinimumDayOutcome.PARTIAL
        }

    val isMet: Boolean get() = outcome == MinimumDayOutcome.MET
}

/**
 * Evalúa el Día Mínimo de un día lógico. Puro: no sabe qué hora es ni en qué zona estás.
 * Es la única fuente del anillo X/N y de "mínimo cumplido" en Hoy.
 */
object MinimumDayEvaluator {

    fun evaluate(day: LocalDate, habits: List<Habit>, completions: List<Completion>): MinimumDayEvaluation {
        val slots = RequiredSlotsForDay.compute(day, habits, completions)
        val doneToday = completions
            .filter { it.day == day && it.status.countsAsDone }
            .mapTo(mutableSetOf()) { it.slotId }
        return MinimumDayEvaluation(
            day = day,
            requiredSlotIds = slots.required,
            doneSlotIds = slots.required intersect doneToday,
            skippedSlotIds = slots.skipped,
        )
    }
}

enum class MinimumSizeCheck { TOO_FEW, OK, TOO_MANY }

/**
 * El mínimo tiene de 3 a 5 elementos. Un elemento es un hábito, no un momento: la medicación
 * con dos tomas cuenta 1. Es un límite suave que muestran onboarding y "Mi mínimo"; el evaluador
 * funciona igual con 2 o con 6.
 */
object MinimumSize {
    const val MIN_ITEMS: Int = 3
    const val MAX_ITEMS: Int = Habit.SOFT_MINIMUM_LIMIT

    fun check(habits: List<Habit>, on: LocalDate): MinimumSizeCheck {
        val count = habits.count { it.countsForMinimum && (it.archivedOn == null || on < it.archivedOn) }
        return when {
            count < MIN_ITEMS -> MinimumSizeCheck.TOO_FEW
            count > MAX_ITEMS -> MinimumSizeCheck.TOO_MANY
            else -> MinimumSizeCheck.OK
        }
    }
}
