package studio.mialdea.floorday.core.domain.model

enum class ReminderMode(val key: String) {
    /** No reminder. */
    NONE("none"),

    /** Included in the daily gentle reminder (at most 1 per day). Needs no time of its own. */
    GENTLE("gentle"),

    /** Exact alarm at the slot's time, with retries. Medication only. */
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
