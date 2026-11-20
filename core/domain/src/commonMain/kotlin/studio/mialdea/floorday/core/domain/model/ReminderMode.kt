package studio.mialdea.floorday.core.domain.model

enum class ReminderMode(val key: String) {
    /** Sin aviso. */
    NONE("none"),

    /** Entra en el aviso suave diario (como máximo 1 al día). No necesita hora propia. */
    GENTLE("gentle"),

    /** Alarma exacta a la hora del momento, con reintentos. Solo medicación. */
    INSISTENT("insistent"),
    ;

    val requiresTime: Boolean
        get() = when (this) {
            NONE, GENTLE -> false
            INSISTENT -> true
        }

    companion object {
        fun fromKey(key: String): ReminderMode? = entries.firstOrNull { it.key == key }
    }
}
