package studio.mialdea.floorday.core.domain.model

import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** Identifier of a habit. Wraps the Uuid so it is not confused with a [SlotId]. */
@JvmInline
value class HabitId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        /** Throws IllegalArgumentException if the text is not a valid UUID. */
        fun parse(text: String): HabitId = HabitId(Uuid.parse(text))
    }
}

/** Identifier of a slot (dose or check) within a habit. */
@JvmInline
value class SlotId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        fun parse(text: String): SlotId = SlotId(Uuid.parse(text))
    }
}

/**
 * Injectable id source. In production it is [Random]; in tests, a fixed sequence
 * so that ids are deterministic.
 */
fun interface IdGenerator {
    fun newUuid(): Uuid

    companion object {
        /** Uuid.random() is stable in Kotlin 2.4: it needs no opt-in. */
        val Random: IdGenerator = IdGenerator { Uuid.random() }
    }
}

fun IdGenerator.newHabitId(): HabitId = HabitId(newUuid())

fun IdGenerator.newSlotId(): SlotId = SlotId(newUuid())
