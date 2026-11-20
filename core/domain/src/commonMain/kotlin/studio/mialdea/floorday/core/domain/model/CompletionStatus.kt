package studio.mialdea.floorday.core.domain.model

/** Undo is not a status: it deletes the (slotId, day) row. */
enum class CompletionStatus(val key: String) {
    /** Done or taken. */
    DONE("done"),

    /** Skipped on purpose (medication only): it leaves the day's required set. */
    SKIPPED("skipped"),
    ;

    val countsAsDone: Boolean get() = this == DONE

    val excusesSlot: Boolean get() = this == SKIPPED

    companion object {
        fun fromKey(key: String): CompletionStatus? = entries.firstOrNull { it.key == key }
    }
}
