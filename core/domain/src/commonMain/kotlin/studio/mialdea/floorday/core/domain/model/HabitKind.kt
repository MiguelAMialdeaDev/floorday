package studio.mialdea.floorday.core.domain.model

/**
 * Tipo de hábito. COUNT y DURATION llegan en v1.1: al añadirlos, cada `when` de
 * abajo deja de compilar hasta decidir su comportamiento. Como se guarda [key] como
 * texto, añadir entradas no exige migrar la columna.
 */
enum class HabitKind(val key: String) {
    CHECK("check"),
    MEDICATION("medication"),
    ;

    /** "Omitida a propósito": neutra para el Día Mínimo. */
    val allowsSkip: Boolean
        get() = when (this) {
            CHECK -> false
            MEDICATION -> true
        }

    /** Cantidad distinta de 1 en sus momentos. */
    val allowsAmount: Boolean
        get() = when (this) {
            CHECK -> false
            MEDICATION -> true
        }

    /** Unidad en texto libre ("pastilla", "gotas"). */
    val allowsUnitLabel: Boolean
        get() = when (this) {
            CHECK -> false
            MEDICATION -> true
        }

    /** Alarma exacta con reintentos (canal dose_alarm). */
    val allowsInsistentReminder: Boolean
        get() = when (this) {
            CHECK -> false
            MEDICATION -> true
        }

    companion object {
        fun fromKey(key: String): HabitKind? = entries.firstOrNull { it.key == key }
    }
}
