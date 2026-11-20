package studio.mialdea.floorday.core.domain.model

import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.time.DayStart

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
    /** Inicio del día lógico; un valor guardado fuera de 00:00 a 06:00 vuelve a 04:00. */
    val dayStart: DayStart get() = DayStart.fromStoredOrDefault(dayStartMinutes)

    /** Normaliza lo leído del almacenamiento. Las reglas de cada campo viven en su tipo. */
    fun sanitized(): UserSettings = copy(dayStartMinutes = dayStart.minutesOfDay)

    companion object {
        const val DEFAULT_DAY_START_MINUTES = DayStart.DEFAULT_MINUTES
        const val DEFAULT_INSIST_INTERVAL_MIN = 10
        const val DEFAULT_INSIST_MAX_RETRIES = 3
    }
}
