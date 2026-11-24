package studio.mialdea.floorday.feature.history

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import studio.mialdea.floorday.core.designsystem.mvi.MviViewModel
import studio.mialdea.floorday.core.domain.time.LogicalDayProvider
import studio.mialdea.floorday.core.domain.usecase.ObserveHabitWeekUseCase

/** History: the last 7 days of every habit. Read only; it follows Room and the logical day. */
class HistoryViewModel(
    observeWeek: ObserveHabitWeekUseCase,
    private val logicalDay: LogicalDayProvider,
) : MviViewModel<HistoryState, HistoryIntent, HistoryEffect>(HistoryState()) {

    init {
        viewModelScope.launch {
            observeWeek().collect { week -> reduce { HistoryReducer.reduce(this, week) } }
        }
    }

    override suspend fun handle(intent: HistoryIntent) {
        when (intent) {
            HistoryIntent.Resumed -> logicalDay.refresh()
        }
    }
}
