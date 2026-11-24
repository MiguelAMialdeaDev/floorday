package studio.mialdea.floorday.feature.history.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.uuid.Uuid
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme
import studio.mialdea.floorday.core.domain.history.HabitDayMark.DONE
import studio.mialdea.floorday.core.domain.history.HabitDayMark.MISSED
import studio.mialdea.floorday.core.domain.history.HabitDayMark.OPEN
import studio.mialdea.floorday.core.domain.history.HabitDayMark.REST
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.feature.history.HistoryRow
import studio.mialdea.floorday.feature.history.HistoryScreen
import studio.mialdea.floorday.feature.history.HistoryState

// Generic sample texts on purpose: no real medication or names (the repo is public).

private val today = LocalDate(2026, 12, 7)

private val week = HistoryState(
    loading = false,
    days = (6 downTo 0).map { today.minus(it, DateTimeUnit.DAY) },
    rows = listOf(
        HistoryRow(HabitId(Uuid.fromLongs(0L, 1L)), "Vitamina D", listOf(DONE, DONE, MISSED, DONE, DONE, DONE, OPEN)),
        HistoryRow(HabitId(Uuid.fromLongs(0L, 2L)), "Caminar 10 minutos", listOf(DONE, MISSED, DONE, DONE, REST, REST, DONE)),
        HistoryRow(HabitId(Uuid.fromLongs(0L, 3L)), "Leer 5 páginas", listOf(REST, REST, REST, MISSED, DONE, DONE, OPEN)),
    ),
)

@Preview(name = "Historial claro", widthDp = 360, heightDp = 760)
@Composable
private fun HistoryLightPreview() {
    FloordayTheme(darkTheme = false) { HistoryScreen(week) }
}

@Preview(name = "Historial oscuro", widthDp = 360, heightDp = 760)
@Composable
private fun HistoryDarkPreview() {
    FloordayTheme(darkTheme = true) { HistoryScreen(week) }
}

@Preview(name = "Historial fuente 200 %", widthDp = 360, heightDp = 1400, fontScale = 2f)
@Composable
private fun HistoryFontScalePreview() {
    FloordayTheme(darkTheme = false) { HistoryScreen(week) }
}

@Preview(name = "Historial vacío", widthDp = 360, heightDp = 400)
@Composable
private fun HistoryEmptyPreview() {
    FloordayTheme(darkTheme = false) { HistoryScreen(HistoryState(loading = false)) }
}
