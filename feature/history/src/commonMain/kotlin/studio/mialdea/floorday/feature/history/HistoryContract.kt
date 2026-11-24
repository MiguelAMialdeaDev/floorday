package studio.mialdea.floorday.feature.history

import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.history.HabitDayMark
import studio.mialdea.floorday.core.domain.history.HabitWeek
import studio.mialdea.floorday.core.domain.model.HabitId

/** One habit of the grid: its name and one mark per day of [HistoryState.days]. */
data class HistoryRow(
    val habitId: HabitId,
    val name: String,
    val marks: List<HabitDayMark>,
)

data class HistoryState(
    val loading: Boolean = true,
    /** Oldest day first; the last one is today. */
    val days: List<LocalDate> = emptyList(),
    val rows: List<HistoryRow> = emptyList(),
) {
    val isEmpty: Boolean get() = !loading && rows.isEmpty()
}

sealed interface HistoryIntent {
    /** ON_RESUME: the logical day may have changed while the app was in the background. */
    data object Resumed : HistoryIntent
}

/** Nothing one-off happens on this screen yet. */
sealed interface HistoryEffect

/** Pure: the domain week becomes what the screen draws. */
object HistoryReducer {
    fun reduce(state: HistoryState, week: HabitWeek): HistoryState = state.copy(
        loading = false,
        days = week.days,
        rows = week.rows.map { row ->
            HistoryRow(habitId = row.habit.id, name = row.habit.name, marks = row.days.map { it.mark })
        },
    )
}
