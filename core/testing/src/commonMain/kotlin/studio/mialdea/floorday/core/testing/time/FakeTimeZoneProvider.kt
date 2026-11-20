package studio.mialdea.floorday.core.testing.time

import kotlin.concurrent.Volatile
import kotlinx.datetime.TimeZone
import studio.mialdea.floorday.core.domain.time.TimeZoneProvider

/** Zona que el test cambia en caliente para simular un viaje. */
class FakeTimeZoneProvider(initial: TimeZone = TestZones.MADRID) : TimeZoneProvider {

    @Volatile
    var zone: TimeZone = initial

    /** Veces que se ha pedido la zona: sirve para comprobar que nadie la guarda. */
    var calls: Int = 0
        private set

    override fun current(): TimeZone {
        calls++
        return zone
    }
}
