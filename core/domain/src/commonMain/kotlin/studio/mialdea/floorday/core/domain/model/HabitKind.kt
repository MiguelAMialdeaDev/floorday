package studio.mialdea.floorday.core.domain.model

/**
 * Habit kind. COUNT and DURATION arrive in v1.1: when they are added, every `when`
 * below stops compiling until their behaviour is decided. Since [key] is stored as
 * text, adding entries does not require migrating the column.
 */
enum class HabitKind(val key: String) {
    CHECK("check"),
    MEDICATION("medication"),
    ;

    /** "Skipped on purpose": neutral for the Minimum Day. */
    val allowsSkip: Boolean
        get() = when (this) {
            CHECK -> false
            MEDICATION -> true
        }

    /** An amount other than 1 in its slots. */
    val allowsAmount: Boolean
        get() = when (this) {
            CHECK -> false
            MEDICATION -> true
        }

    /** Free-text unit ("pill", "drops"). */
    val allowsUnitLabel: Boolean
        get() = when (this) {
            CHECK -> false
            MEDICATION -> true
        }

    /** Exact alarm with retries (dose_alarm channel). */
    val allowsInsistentReminder: Boolean
        get() = when (this) {
            CHECK -> false
            MEDICATION -> true
        }

    companion object {
        fun fromKey(key: String): HabitKind? = entries.firstOrNull { it.key == key }
    }
}
