package studio.mialdea.floorday.shared.di

import kotlin.time.Clock
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.TimeZone
import org.koin.dsl.module
import studio.mialdea.floorday.core.domain.time.ClockLogicalDayProvider
import studio.mialdea.floorday.core.domain.time.DayStart
import studio.mialdea.floorday.core.domain.time.LogicalDayProvider
import studio.mialdea.floorday.core.domain.time.TimeZoneProvider

/** The only place that touches the system clock and zone (AGENTS.md, hard rule 2). */
val timeModule = module {
    single<Clock> { Clock.System }
    // A lambda, not a stored zone: a trip changes it while the app is running.
    single<TimeZoneProvider> { TimeZoneProvider { TimeZone.currentSystemDefault() } }
    // single: refresh() on ON_RESUME must reach the same instance Today observes.
    // The day start is fixed at 04:00 until the setting is editable (S1).
    single<LogicalDayProvider> { ClockLogicalDayProvider(get(), get(), flowOf(DayStart.Default)) }
}
