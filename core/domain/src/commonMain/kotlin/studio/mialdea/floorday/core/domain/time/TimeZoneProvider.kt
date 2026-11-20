package studio.mialdea.floorday.core.domain.time

import kotlinx.datetime.TimeZone

/**
 * Current time zone of the device. It is queried on every computation and never stored:
 * on a trip it changes without the app restarting.
 */
fun interface TimeZoneProvider {
    fun current(): TimeZone
}
