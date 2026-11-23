package studio.mialdea.floorday.core.domain.platform

import kotlinx.datetime.LocalTime

/** A time as the system shows it: 12 or 24 h and the locale's order. Android impl in :androidApp. */
fun interface TimeOfDayFormatter {
    fun format(time: LocalTime): String
}
