package studio.mialdea.floorday.feature.today.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.feature.today.editor.HabitEditorMapper
import studio.mialdea.floorday.feature.today.editor.HabitEditorScreen
import studio.mialdea.floorday.feature.today.editor.HabitEditorState
import studio.mialdea.floorday.feature.today.editor.HabitEditorValidator
import studio.mialdea.floorday.feature.today.editor.SlotDraft

// Generic sample texts on purpose: no real medication or names (the repo is public).

private val medication = HabitEditorMapper.newHabit().copy(
    habitId = HabitId(Uuid.fromLongs(0L, 1L)),
    name = "Vitamina D",
    kind = HabitKind.MEDICATION,
    unitLabel = "pastilla",
    slots = listOf(
        SlotDraft(0, label = "Mañana", time = LocalTime(8, 0), reminder = ReminderMode.INSISTENT),
        SlotDraft(1, label = "Noche", time = LocalTime(22, 0), amount = Quarters.HALF, reminder = ReminderMode.GENTLE),
    ),
    nextKey = 2,
)

private val withErrors = HabitEditorMapper.newHabit().let { it.copy(submitted = true, errors = HabitEditorValidator.validate(it)) }

@Composable
private fun EditorSample(state: HabitEditorState) {
    HabitEditorScreen(state = state, onIntent = {}, formatTime = { it.toString() }, is24Hour = true)
}

@Preview(name = "Editor medicación claro", widthDp = 360, heightDp = 1100)
@Composable
private fun EditorMedicationLightPreview() {
    FloordayTheme(darkTheme = false) { EditorSample(medication) }
}

@Preview(name = "Editor medicación oscuro", widthDp = 360, heightDp = 1100)
@Composable
private fun EditorMedicationDarkPreview() {
    FloordayTheme(darkTheme = true) { EditorSample(medication) }
}

@Preview(name = "Editor fuente 200 %", widthDp = 360, heightDp = 2000, fontScale = 2f)
@Composable
private fun EditorFontScalePreview() {
    FloordayTheme(darkTheme = false) { EditorSample(medication) }
}

@Preview(name = "Editor nuevo con errores", widthDp = 360, heightDp = 800)
@Composable
private fun EditorErrorsPreview() {
    FloordayTheme(darkTheme = false) { EditorSample(withErrors) }
}
