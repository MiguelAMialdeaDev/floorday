package studio.mialdea.floorday.core.domain.model

import kotlin.jvm.JvmInline
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

/**
 * Conjunto de días de la semana guardado como bitmask fijo L..D: lunes es el bit 0
 * (valor 1) y domingo el bit 6 (valor 64). Ese orden es un contrato con la BD y el
 * JSON exportado: no depende del locale. La UI decide qué día pinta primero.
 *
 * Puede estar vacío para que el borrador del editor lo represente; un [Habit] no.
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

    /** Días en orden ISO (lunes primero). */
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
