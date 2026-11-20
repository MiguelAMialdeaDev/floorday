package studio.mialdea.floorday.core.domain.model

import kotlinx.datetime.LocalTime

enum class WeightUnit { KG, LB, ST }

/** Ajustes del usuario (DataStore "settings"). Viajan en la copia de seguridad. */
data class UserSettings(
    val onboardingDone: Boolean = false,
    val dayStartMinutes: Int = DEFAULT_DAY_START_MINUTES,
    /** Hora del aviso suave diario; null = sin aviso suave. */
    val gentleReminderTime: LocalTime? = null,
    val insistIntervalMin: Int = DEFAULT_INSIST_INTERVAL_MIN,
    val insistMaxRetries: Int = DEFAULT_INSIST_MAX_RETRIES,
    val showNameOnLockScreen: Boolean = false,
    /** null = según el locale. */
    val weightUnit: WeightUnit? = null,
    val haptics: Boolean = true,
) {
    companion object {
        const val DEFAULT_DAY_START_MINUTES = 240
        const val DEFAULT_INSIST_INTERVAL_MIN = 10
        const val DEFAULT_INSIST_MAX_RETRIES = 3
    }
}
