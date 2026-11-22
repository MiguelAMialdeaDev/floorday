package studio.mialdea.floorday.core.domain.model

import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.time.DayStart

/** [key] is stored in DataStore ("user_settings"): never change an existing one. */
enum class WeightUnit(val key: String) {
    KG("kg"),
    LB("lb"),
    ST("st"),
    ;

    companion object {
        fun fromKey(key: String): WeightUnit? = entries.firstOrNull { it.key == key }
    }
}

/** User settings (DataStore "user_settings"). They travel in the backup. */
data class UserSettings(
    val onboardingDone: Boolean = false,
    val dayStartMinutes: Int = DEFAULT_DAY_START_MINUTES,
    /** Time of the daily gentle reminder; null = no gentle reminder. */
    val gentleReminderTime: LocalTime? = null,
    val insistIntervalMin: Int = DEFAULT_INSIST_INTERVAL_MIN,
    val insistMaxRetries: Int = DEFAULT_INSIST_MAX_RETRIES,
    val showNameOnLockScreen: Boolean = false,
    /** null = follow the locale. */
    val weightUnit: WeightUnit? = null,
    val haptics: Boolean = true,
) {
    /** Start of the logical day; a stored value outside 00:00 to 06:00 falls back to 04:00. */
    val dayStart: DayStart get() = DayStart.fromStoredOrDefault(dayStartMinutes)

    /** Normalizes what is read from storage. Each field's rules live in its type. */
    fun sanitized(): UserSettings = copy(dayStartMinutes = dayStart.minutesOfDay)

    companion object {
        const val DEFAULT_DAY_START_MINUTES = DayStart.DEFAULT_MINUTES
        const val DEFAULT_INSIST_INTERVAL_MIN = 10
        const val DEFAULT_INSIST_MAX_RETRIES = 3
    }
}
