package studio.mialdea.floorday.core.domain.model

/** Deshacer no es un estado: borra la fila de (slotId, day). */
enum class CompletionStatus(val key: String) {
    /** Hecho o tomada. */
    DONE("done"),

    /** Omitida a propósito (solo medicación): sale de los requeridos del día. */
    SKIPPED("skipped"),
    ;

    val countsAsDone: Boolean get() = this == DONE

    val excusesSlot: Boolean get() = this == SKIPPED

    companion object {
        fun fromKey(key: String): CompletionStatus? = entries.firstOrNull { it.key == key }
    }
}
