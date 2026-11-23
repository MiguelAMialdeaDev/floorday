package studio.mialdea.floorday.feature.today.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.designsystem.component.CheckState
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.SlotId
import studio.mialdea.floorday.feature.today.DayPart
import studio.mialdea.floorday.feature.today.TodayContent
import studio.mialdea.floorday.feature.today.TodayRow
import studio.mialdea.floorday.feature.today.TodaySection
import studio.mialdea.floorday.feature.today.TodayScreen
import studio.mialdea.floorday.feature.today.TodayState

// Generic sample texts on purpose: no real medication or names (the repo is public).

private fun row(n: Int, title: String, supporting: String?, state: CheckState, doneTime: String? = null, minimum: Boolean = true) =
    TodayRow(SlotId(Uuid.fromLongs(0L, n.toLong())), HabitId(Uuid.fromLongs(1L, n.toLong())), title, supporting, doneTime, state, minimum)

private val sample = TodayContent(
    day = LocalDate(2026, 12, 1),
    sections = listOf(
        TodaySection(
            DayPart.MORNING,
            listOf(
                row(1, "Vitamina D", "Mañana · 08:00 · 1 pastilla", CheckState.Done, doneTime = "08:42"),
                row(2, "Beber agua", "09:00", CheckState.Pending),
            ),
        ),
        TodaySection(DayPart.AFTERNOON, listOf(row(3, "Caminar 10 minutos", null, CheckState.Pending))),
        TodaySection(DayPart.NIGHT, listOf(row(4, "Vitamina D", "Noche · 22:00 · ½ pastilla", CheckState.Skipped))),
    ),
    others = listOf(row(5, "Estirar", null, CheckState.Pending, minimum = false)),
    hasHabits = true,
)

@Composable
private fun TodaySample(state: TodayState) {
    TodayScreen(state = state, onIntent = {}, onOpenHistory = {}, onOpenSettings = {}, onOpenEditor = {})
}

@Preview(name = "Hoy claro", widthDp = 360, heightDp = 900)
@Composable
private fun TodayLightPreview() {
    FloordayTheme(darkTheme = false) { TodaySample(TodayState(isLoading = false, content = sample, othersExpanded = true)) }
}

@Preview(name = "Hoy oscuro", widthDp = 360, heightDp = 900)
@Composable
private fun TodayDarkPreview() {
    FloordayTheme(darkTheme = true) { TodaySample(TodayState(isLoading = false, content = sample)) }
}

@Preview(name = "Hoy fuente 200 %", widthDp = 360, heightDp = 1600, fontScale = 2f)
@Composable
private fun TodayFontScalePreview() {
    FloordayTheme(darkTheme = false) { TodaySample(TodayState(isLoading = false, content = sample)) }
}

@Preview(name = "Hoy sin hábitos", widthDp = 360, heightDp = 640)
@Composable
private fun TodayEmptyPreview() {
    FloordayTheme(darkTheme = false) {
        TodaySample(TodayState(isLoading = false, content = sample.copy(sections = emptyList(), others = emptyList(), hasHabits = false)))
    }
}
