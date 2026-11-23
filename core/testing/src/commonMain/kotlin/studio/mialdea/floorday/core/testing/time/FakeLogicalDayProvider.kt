package studio.mialdea.floorday.core.testing.time

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.time.LogicalDayProvider

/** Logical day the test moves by hand: set [current] to start a new day. */
class FakeLogicalDayProvider(start: LocalDate) : LogicalDayProvider {
    val current = MutableStateFlow(start)

    /** Times refresh() was called (ON_RESUME). */
    var refreshes: Int = 0
        private set

    override fun observe(): Flow<LocalDate> = current

    override fun refresh() {
        refreshes++
    }
}
