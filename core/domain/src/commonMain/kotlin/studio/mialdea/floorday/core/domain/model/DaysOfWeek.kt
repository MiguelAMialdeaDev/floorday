package studio.mialdea.floorday.core.domain.model

import kotlin.jvm.JvmInline
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

/**
 * Set of weekdays stored as a fixed Mon..Sun bitmask: Monday is bit 0
 * (value 1) and Sunday is bit 6 (value 64). That order is a contract with the DB and the
 * exported JSON: it does not depend on the locale. The UI decides which day it draws first.
 *
 * It can be empty so the editor draft can represent it; a [Habit] cannot.
 */
@JvmInline
value class DaysOfWeek(val mask: Int) {

    init {
        require(mask in 0..ALL_MASK) { "Bitmask de días fuera de rango: $mask" }
    }

    operator fun contains(day: DayOfWeek): Boolean = mask and bit(day) != 0

    val isEmpty: Boolean get() = mask == 0

    val size: Int get() = mask.countOneBits()

    operator fun plus(day: DayOfWeek): DaysOfWeek = DaysOfWeek(mask or bit(day))

    operator fun minus(day: DayOfWeek): DaysOfWeek = DaysOfWeek(mask and bit(day).inv())

    fun toggle(day: DayOfWeek): DaysOfWeek = if (day in this) this - day else this + day

    /** Days in ISO order (Monday first). */
    fun toSet(): Set<DayOfWeek> = DayOfWeek.entries.filterTo(LinkedHashSet()) { it in this }

    override fun toString(): String = "DaysOfWeek(${toSet().joinToString()})"

    companion object {
        private const val ALL_MASK = 0b111_1111

        val NONE = DaysOfWeek(0)
        val EVERY_DAY = DaysOfWeek(ALL_MASK)
        val WEEKDAYS = of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
        )

        fun of(vararg days: DayOfWeek): DaysOfWeek = of(days.asIterable())

        fun of(days: Iterable<DayOfWeek>): DaysOfWeek =
            DaysOfWeek(days.fold(0) { acc, day -> acc or bit(day) })

        private fun bit(day: DayOfWeek): Int = 1 shl (day.isoDayNumber - 1)
    }
}
