package studio.mialdea.floorday.core.domain.platform

import kotlinx.datetime.LocalTime

/** A time as the system shows it: 12 or 24 h and the locale's order. Android impl in :androidApp. */
fun interface TimeOfDayFormatter {
    fun format(time: LocalTime): String

    /**
     * The system clock is in 24 h. The time picker needs it so it shows the same format as
     * [format]. Read it again on resume: changing it in the system settings is not a config change.
     */
    fun is24Hour(): Boolean = true
}
