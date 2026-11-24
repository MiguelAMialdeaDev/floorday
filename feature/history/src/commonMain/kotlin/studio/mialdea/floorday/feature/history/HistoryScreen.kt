package studio.mialdea.floorday.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import studio.mialdea.floorday.core.designsystem.component.DayMark
import studio.mialdea.floorday.core.designsystem.component.DayMarkState
import studio.mialdea.floorday.core.designsystem.theme.FloordayDimens
import studio.mialdea.floorday.core.domain.history.HabitDayMark
import studio.mialdea.floorday.feature.history.resources.Res
import studio.mialdea.floorday.feature.history.resources.history_day_full_friday
import studio.mialdea.floorday.feature.history.resources.history_day_full_monday
import studio.mialdea.floorday.feature.history.resources.history_day_full_saturday
import studio.mialdea.floorday.feature.history.resources.history_day_full_sunday
import studio.mialdea.floorday.feature.history.resources.history_day_full_thursday
import studio.mialdea.floorday.feature.history.resources.history_day_full_tuesday
import studio.mialdea.floorday.feature.history.resources.history_day_full_wednesday
import studio.mialdea.floorday.feature.history.resources.history_day_mark
import studio.mialdea.floorday.feature.history.resources.history_day_short_friday
import studio.mialdea.floorday.feature.history.resources.history_day_short_monday
import studio.mialdea.floorday.feature.history.resources.history_day_short_saturday
import studio.mialdea.floorday.feature.history.resources.history_day_short_sunday
import studio.mialdea.floorday.feature.history.resources.history_day_short_thursday
import studio.mialdea.floorday.feature.history.resources.history_day_short_tuesday
import studio.mialdea.floorday.feature.history.resources.history_day_short_wednesday
import studio.mialdea.floorday.feature.history.resources.history_empty
import studio.mialdea.floorday.feature.history.resources.history_mark_done
import studio.mialdea.floorday.feature.history.resources.history_mark_missed
import studio.mialdea.floorday.feature.history.resources.history_mark_open
import studio.mialdea.floorday.feature.history.resources.history_mark_rest
import studio.mialdea.floorday.feature.history.resources.history_title
import studio.mialdea.floorday.feature.history.resources.history_week_heading

/** Stateless: the week grid, one block per habit (name above its 7 squares). */
@Composable
internal fun HistoryScreen(state: HistoryState, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = FloordayDimens.gutter, vertical = FloordayDimens.spaceXl),
            verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceL),
        ) {
            item(key = "title") {
                Text(
                    text = stringResource(Res.string.history_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
            }
            when {
                state.isEmpty -> item(key = "empty") {
                    Text(stringResource(Res.string.history_empty), style = MaterialTheme.typography.bodyLarge)
                }
                !state.loading -> {
                    item(key = "week") { WeekHeader(state.days) }
                    items(state.rows, key = { it.habitId.toString() }) { row -> HabitWeekBlock(row, state.days) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekHeader(days: List<LocalDate>) {
    Column(verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceM)) {
        Text(
            text = stringResource(Res.string.history_week_heading),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        // Legend: the same shapes as the grid, so nothing depends on knowing the colours.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS),
        ) {
            HabitDayMark.entries.forEach { mark -> LegendItem(mark) }
        }
        // Column labels; TalkBack skips them because every square says its own day.
        DayColumns(count = days.size, modifier = Modifier.clearAndSetSemantics { }) { index ->
            val day = days[index]
            val isToday = index == days.lastIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(shortName(day.dayOfWeek)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = day.day.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun LegendItem(mark: HabitDayMark) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS),
    ) {
        DayMark(mark.toDayMarkState(), modifier = Modifier.size(FloordayDimens.checkGlyph))
        Text(stringResource(markLabel(mark)), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HabitWeekBlock(row: HistoryRow, days: List<LocalDate>) {
    Column(verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS)) {
        Text(
            text = row.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        DayColumns(count = days.size) { index ->
            val day = days[index]
            val description = stringResource(
                Res.string.history_day_mark,
                stringResource(fullName(day.dayOfWeek)),
                day.day,
                stringResource(markLabel(row.marks[index])),
            )
            DayMark(
                mark = row.marks[index].toDayMarkState(),
                // Grows with the screen but never past 48 dp, so a tablet does not get huge squares.
                modifier = Modifier.widthIn(max = FloordayDimens.spaceXxxl).fillMaxWidth()
                    .semantics { contentDescription = description },
            )
        }
    }
}

/** Seven equal columns, shared by the header and every habit so they line up. */
@Composable
private fun DayColumns(count: Int, modifier: Modifier = Modifier, cell: @Composable (index: Int) -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS),
    ) {
        repeat(count) { index ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { cell(index) }
        }
    }
}

private fun markLabel(mark: HabitDayMark): StringResource = when (mark) {
    HabitDayMark.DONE -> Res.string.history_mark_done
    HabitDayMark.MISSED -> Res.string.history_mark_missed
    HabitDayMark.OPEN -> Res.string.history_mark_open
    HabitDayMark.REST -> Res.string.history_mark_rest
}

private fun shortName(day: DayOfWeek): StringResource = when (day) {
    DayOfWeek.MONDAY -> Res.string.history_day_short_monday
    DayOfWeek.TUESDAY -> Res.string.history_day_short_tuesday
    DayOfWeek.WEDNESDAY -> Res.string.history_day_short_wednesday
    DayOfWeek.THURSDAY -> Res.string.history_day_short_thursday
    DayOfWeek.FRIDAY -> Res.string.history_day_short_friday
    DayOfWeek.SATURDAY -> Res.string.history_day_short_saturday
    DayOfWeek.SUNDAY -> Res.string.history_day_short_sunday
}

private fun fullName(day: DayOfWeek): StringResource = when (day) {
    DayOfWeek.MONDAY -> Res.string.history_day_full_monday
    DayOfWeek.TUESDAY -> Res.string.history_day_full_tuesday
    DayOfWeek.WEDNESDAY -> Res.string.history_day_full_wednesday
    DayOfWeek.THURSDAY -> Res.string.history_day_full_thursday
    DayOfWeek.FRIDAY -> Res.string.history_day_full_friday
    DayOfWeek.SATURDAY -> Res.string.history_day_full_saturday
    DayOfWeek.SUNDAY -> Res.string.history_day_full_sunday
}

private fun HabitDayMark.toDayMarkState(): DayMarkState = when (this) {
    HabitDayMark.DONE -> DayMarkState.DONE
    HabitDayMark.MISSED -> DayMarkState.MISSED
    HabitDayMark.OPEN -> DayMarkState.OPEN
    HabitDayMark.REST -> DayMarkState.REST
}
