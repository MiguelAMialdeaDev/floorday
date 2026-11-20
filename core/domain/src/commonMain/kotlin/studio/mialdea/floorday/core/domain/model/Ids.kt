package studio.mialdea.floorday.core.domain.model

import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** Identificador de un hábito. Envuelve el Uuid para no confundirlo con un [SlotId]. */
@JvmInline
value class HabitId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        /** Lanza IllegalArgumentException si el texto no es un UUID válido. */
        fun parse(text: String): HabitId = HabitId(Uuid.parse(text))
    }
}

/** Identificador de un momento (toma o check) dentro de un hábito. */
@JvmInline
value class SlotId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        fun parse(text: String): SlotId = SlotId(Uuid.parse(text))
    }
}

/**
 * Origen de ids inyectable. En producción es [Random]; en tests, una secuencia fija
 * para que los ids sean deterministas.
 */
fun interface IdGenerator {
    fun newUuid(): Uuid

    companion object {
        /** Uuid.random() es estable en Kotlin 2.4: no necesita opt-in. */
        val Random: IdGenerator = IdGenerator { Uuid.random() }
    }
}

fun IdGenerator.newHabitId(): HabitId = HabitId(newUuid())

fun IdGenerator.newSlotId(): SlotId = SlotId(newUuid())
