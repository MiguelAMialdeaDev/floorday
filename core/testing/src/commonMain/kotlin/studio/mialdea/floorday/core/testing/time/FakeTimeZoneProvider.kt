package studio.mialdea.floorday.core.testing.time

import kotlin.concurrent.Volatile
import kotlinx.datetime.TimeZone
import studio.mialdea.floorday.core.domain.time.TimeZoneProvider

/** Zone the test changes on the fly to simulate a trip. */
class FakeTimeZoneProvider(initial: TimeZone = TestZones.MADRID) : TimeZoneProvider {

    @Volatile
    var zone: TimeZone = initial

    /** Times the zone has been requested: used to check that nobody stores it. */
    var calls: Int = 0
        private set

    override fun current(): TimeZone {
        calls++
        return zone
    }
}
