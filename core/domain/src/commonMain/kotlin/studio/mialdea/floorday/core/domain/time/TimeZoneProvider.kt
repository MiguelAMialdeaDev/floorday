package studio.mialdea.floorday.core.domain.time

import kotlinx.datetime.TimeZone

/**
 * Zona horaria actual del dispositivo. Se consulta en cada cálculo y nunca se guarda:
 * en un viaje cambia sin que la app se reinicie.
 */
fun interface TimeZoneProvider {
    fun current(): TimeZone
}
