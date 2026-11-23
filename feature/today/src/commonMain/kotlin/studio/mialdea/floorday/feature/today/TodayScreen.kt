package studio.mialdea.floorday.feature.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import studio.mialdea.floorday.core.designsystem.component.CheckRow
import studio.mialdea.floorday.core.designsystem.component.ProgressRing
import studio.mialdea.floorday.core.designsystem.theme.FloordayDimens
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.feature.today.resources.Res
import studio.mialdea.floorday.feature.today.resources.today_collapsed
import studio.mialdea.floorday.feature.today.resources.today_edit_habit
import studio.mialdea.floorday.feature.today.resources.today_new_habit
import studio.mialdea.floorday.feature.today.resources.today_empty_body
import studio.mialdea.floorday.feature.today.resources.today_empty_title
import studio.mialdea.floorday.feature.today.resources.today_expanded
import studio.mialdea.floorday.feature.today.resources.today_open_history
import studio.mialdea.floorday.feature.today.resources.today_open_settings
import studio.mialdea.floorday.feature.today.resources.today_others
import studio.mialdea.floorday.feature.today.resources.today_part_afternoon
import studio.mialdea.floorday.feature.today.resources.today_part_anytime
import studio.mialdea.floorday.feature.today.resources.today_part_morning
import studio.mialdea.floorday.feature.today.resources.today_part_night
import studio.mialdea.floorday.feature.today.resources.today_progress
import studio.mialdea.floorday.feature.today.resources.today_rest_day
import studio.mialdea.floorday.feature.today.resources.today_title

/** Stateless: what it draws comes from [state] and every action goes out through [onIntent]. */
@Composable
internal fun TodayScreen(
    state: TodayState,
    onIntent: (TodayIntent) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    /** null = new habit. */
    onOpenEditor: (HabitId?) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        val content = state.content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = FloordayDimens.gutter,
                end = FloordayDimens.gutter,
                top = padding.calculateTopPadding() + FloordayDimens.spaceS,
                bottom = padding.calculateBottomPadding() + FloordayDimens.spaceXxl,
            ),
            verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceXs),
        ) {
            item(key = "title") { TodayTitle(onOpenHistory, onOpenSettings) }
            when {
                // Room answers in milliseconds: an empty frame is calmer than a flashing spinner.
                state.isLoading || content == null -> Unit
                !content.hasHabits -> item(key = "empty") { TodayEmpty(onCreate = { onOpenEditor(null) }) }
                else -> {
                    todayContent(content, state.othersExpanded, onIntent, onOpenEditor)
                    item(key = "new") { NewHabitButton(onClick = { onOpenEditor(null) }) }
                }
            }
        }
    }
}

private fun LazyListScope.todayContent(
    content: TodayContent,
    othersExpanded: Boolean,
    onIntent: (TodayIntent) -> Unit,
    onOpenEditor: (HabitId?) -> Unit,
) {
    item(key = "ring") { TodayRing(content) }
    content.sections.forEach { section ->
        item(key = "part-${section.part.name}") { PartTitle(section.part) }
        items(section.rows, key = { "row-${it.slotId}" }) { row -> TodayCheck(row, onIntent, onOpenEditor) }
    }
    if (content.others.isNotEmpty()) {
        item(key = "others") {
            OthersHeader(content.others.size, othersExpanded, onToggle = { onIntent(TodayIntent.ToggleOthers) })
        }
        if (othersExpanded) {
            items(content.others, key = { "other-${it.slotId}" }) { row -> TodayCheck(row, onIntent, onOpenEditor) }
        }
    }
}

@Composable
private fun TodayTitle(onOpenHistory: () -> Unit, onOpenSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.today_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        TextButton(onClick = onOpenHistory) { Text(stringResource(Res.string.today_open_history)) }
        TextButton(onClick = onOpenSettings) { Text(stringResource(Res.string.today_open_settings)) }
    }
}

@Composable
private fun TodayRing(content: TodayContent) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = FloordayDimens.spaceXl, bottom = FloordayDimens.spaceXxl),
        contentAlignment = Alignment.Center,
    ) {
        if (content.isRestDay) {
            Text(
                text = stringResource(Res.string.today_rest_day),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        } else {
            ProgressRing(
                done = content.done,
                required = content.required,
                stateText = stringResource(Res.string.today_progress, content.done, content.required),
            )
        }
    }
}

@Composable
private fun PartTitle(part: DayPart) {
    Text(
        text = stringResource(part.title()).uppercase(),
        style = FloordayTheme.type.eyebrow,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(top = FloordayDimens.spaceL, bottom = FloordayDimens.spaceXs)
            .semantics { heading() },
    )
}

@Composable
private fun TodayCheck(row: TodayRow, onIntent: (TodayIntent) -> Unit, onOpenEditor: (HabitId?) -> Unit) {
    CheckRow(
        label = row.title,
        state = row.state,
        onToggle = { onIntent(TodayIntent.ToggleSlot(row.slotId)) },
        supportingText = row.supportingText,
        doneTime = row.doneTime,
        partOfMinimum = row.partOfMinimum,
        // For now a long press opens the editor straight away; the sheet with "Edit" and
        // "Skipped on purpose" arrives with skipping (S1).
        onLongClick = { onOpenEditor(row.habitId) },
        longClickLabel = stringResource(Res.string.today_edit_habit),
    )
}

@Composable
private fun NewHabitButton(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(top = FloordayDimens.spaceXl), contentAlignment = Alignment.Center) {
        OutlinedButton(onClick = onClick) { Text(stringResource(Res.string.today_new_habit)) }
    }
}

@Composable
private fun OthersHeader(count: Int, expanded: Boolean, onToggle: () -> Unit) {
    val stateText = stringResource(if (expanded) Res.string.today_expanded else Res.string.today_collapsed)
    Row(
        modifier = Modifier
            .padding(top = FloordayDimens.spaceL)
            .fillMaxWidth()
            .heightIn(min = FloordayDimens.minTouchTarget)
            .clickable(role = Role.Button, onClick = onToggle)
            .semantics { stateDescription = stateText },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.today_others, count),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        Text(text = if (expanded) "▲" else "▼", modifier = Modifier.clearAndSetSemantics { })
    }
}

@Composable
private fun TodayEmpty(onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = FloordayDimens.spaceXxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS),
    ) {
        Text(
            text = stringResource(Res.string.today_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(Res.string.today_empty_body),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onCreate, modifier = Modifier.padding(top = FloordayDimens.spaceL)) {
            Text(stringResource(Res.string.today_new_habit))
        }
    }
}

private fun DayPart.title(): StringResource = when (this) {
    DayPart.MORNING -> Res.string.today_part_morning
    DayPart.AFTERNOON -> Res.string.today_part_afternoon
    DayPart.NIGHT -> Res.string.today_part_night
    DayPart.ANYTIME -> Res.string.today_part_anytime
}
