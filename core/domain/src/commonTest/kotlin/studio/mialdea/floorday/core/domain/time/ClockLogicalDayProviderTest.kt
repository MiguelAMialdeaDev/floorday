package studio.mialdea.floorday.core.domain.time

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.testing.time.FakeClock
import studio.mialdea.floorday.core.testing.time.FakeTimeZoneProvider

class ClockLogicalDayProviderTest {

    private val clock = FakeClock()
    private val provider = ClockLogicalDayProvider(clock, FakeTimeZoneProvider(), flowOf(DayStart.Default))

    @Test
    fun `el dia cambia a las 04 00 y no antes`() = runTest {
        clock.setLocal(LocalDate(2026, 12, 2), LocalTime(3, 59))
        provider.observe().test {
            assertEquals(LocalDate(2026, 12, 1), awaitItem())
            clock.advanceBy(1.minutes)
            assertEquals(LocalDate(2026, 12, 2), awaitItem())
            // It slept exactly until 04:00, not a full wake-up interval.
            assertEquals(1.minutes.inWholeMilliseconds, testScheduler.currentTime)
        }
    }

    @Test
    fun `refresh recoge el tiempo que paso en reposo profundo`() = runTest {
        clock.setLocal(LocalDate(2026, 12, 1), LocalTime(12, 0))
        provider.observe().test {
            assertEquals(LocalDate(2026, 12, 1), awaitItem())
            // The wall clock jumps but the coroutine clock does not move, as in Doze.
            clock.advanceBy(2.days)
            provider.refresh()
            assertEquals(LocalDate(2026, 12, 3), awaitItem())
            assertEquals(0L, testScheduler.currentTime)
        }
    }
}
