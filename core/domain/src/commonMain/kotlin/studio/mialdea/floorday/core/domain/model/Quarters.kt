package studio.mialdea.floorday.core.domain.model

import kotlin.jvm.JvmInline

/**
 * Amount in quarters of a unit: 4 = 1, 2 = ½, 6 = 1 ½.
 *
 * Stored as an Int in the DB (no floats). It has no addition on purpose: two slots
 * of 1 and ½ are two slots for the Minimum Day, not 1.5 units. If a screen
 * needs a total, it adds up each [value] as an Int.
 */
@JvmInline
value class Quarters(val value: Int) : Comparable<Quarters> {

    init {
        require(value in MIN_VALUE..MAX_VALUE) {
            "La cantidad debe estar entre $MIN_VALUE y $MAX_VALUE cuartos, y es $value"
        }
    }

    /** Whole units: 6 quarters are 1. */
    val wholeUnits: Int get() = value / PER_UNIT

    /** Remainder in quarters: 6 quarters leave HALF. */
    val fraction: QuarterFraction get() = QuarterFraction.fromQuarters(value % PER_UNIT)

    val isWhole: Boolean get() = value % PER_UNIT == 0

    override fun compareTo(other: Quarters): Int = value.compareTo(other.value)

    override fun toString(): String = "Quarters($value)"

    companion object {
        const val PER_UNIT = 4
        const val MIN_VALUE = 1

        /** 10 units. The domain allows more than the MVP picker offers. */
        const val MAX_VALUE = 40

        val QUARTER = Quarters(1)
        val HALF = Quarters(2)
        val THREE_QUARTERS = Quarters(3)
        val ONE = Quarters(PER_UNIT)

        /** MVP picker options: from ¼ to 2. */
        val EDITOR_CHOICES: List<Quarters> = (1..2 * PER_UNIT).map { Quarters(it) }

        fun of(wholeUnits: Int, fraction: QuarterFraction = QuarterFraction.NONE): Quarters {
            require(wholeUnits >= 0) { "Las unidades enteras no pueden ser negativas: $wholeUnits" }
            return Quarters(wholeUnits * PER_UNIT + fraction.quarters)
        }
    }
}

enum class QuarterFraction(val quarters: Int) {
    NONE(0),
    QUARTER(1),
    HALF(2),
    THREE_QUARTERS(3),
    ;

    companion object {
        fun fromQuarters(quarters: Int): QuarterFraction =
            entries.firstOrNull { it.quarters == quarters }
                ?: throw IllegalArgumentException("Resto de cuartos fuera de rango: $quarters")
    }
}
