package studio.mialdea.floorday.feature.today.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import studio.mialdea.floorday.core.designsystem.theme.FloordayDimens
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.feature.today.resources.Res
import studio.mialdea.floorday.feature.today.resources.editor_cancel
import studio.mialdea.floorday.feature.today.resources.editor_counts_hint
import studio.mialdea.floorday.feature.today.resources.editor_counts_label
import studio.mialdea.floorday.feature.today.resources.editor_delete
import studio.mialdea.floorday.feature.today.resources.editor_delete_body
import studio.mialdea.floorday.feature.today.resources.editor_delete_confirm
import studio.mialdea.floorday.feature.today.resources.editor_delete_title
import studio.mialdea.floorday.feature.today.resources.editor_error_summary
import studio.mialdea.floorday.feature.today.resources.editor_kind_check
import studio.mialdea.floorday.feature.today.resources.editor_kind_label
import studio.mialdea.floorday.feature.today.resources.editor_kind_medication
import studio.mialdea.floorday.feature.today.resources.editor_name_label
import studio.mialdea.floorday.feature.today.resources.editor_name_placeholder
import studio.mialdea.floorday.feature.today.resources.editor_save
import studio.mialdea.floorday.feature.today.resources.editor_slot_add_check
import studio.mialdea.floorday.feature.today.resources.editor_slot_add_medication
import studio.mialdea.floorday.feature.today.resources.editor_slots_check
import studio.mialdea.floorday.feature.today.resources.editor_slots_medication
import studio.mialdea.floorday.feature.today.resources.editor_title_edit
import studio.mialdea.floorday.feature.today.resources.editor_title_new
import studio.mialdea.floorday.feature.today.resources.editor_unit_label
import studio.mialdea.floorday.feature.today.resources.editor_unit_placeholder
import studio.mialdea.floorday.feature.today.resources.editor_write_failed
// The alias hides the original name in this file, so both imports are needed.
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent as I

/** Stateless: everything comes in [state] and goes out through [onIntent]. This is what previews draw. */
@Composable
internal fun HabitEditorScreen(
    state: HabitEditorState,
    onIntent: (HabitEditorIntent) -> Unit,
    formatTime: (LocalTime) -> String,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
    nameFocusRequester: FocusRequester = remember { FocusRequester() },
) {
    Scaffold(
        modifier = modifier,
        topBar = { EditorTopBar(state, onIntent) },
    ) { padding ->
        if (state.phase == HabitEditorState.Phase.LOADING) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            EditorForm(state, onIntent, formatTime, nameFocusRequester, Modifier.padding(padding))
        }
    }

    state.timePickerSlot?.let { key ->
        val slot = state.slot(key) ?: return@let
        SlotTimePickerDialog(
            initial = slot.time ?: DEFAULT_PICKER_TIME,
            is24Hour = is24Hour,
            onConfirm = { onIntent(I.TimePicked(key, it)) },
            onDismiss = { onIntent(I.TimePickerDismissed) },
        )
    }

    if (state.confirmingDelete) DeleteDialog(state.name.trim(), onIntent)
}

@Composable
private fun EditorTopBar(state: HabitEditorState, onIntent: (HabitEditorIntent) -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(if (state.isNew) Res.string.editor_title_new else Res.string.editor_title_edit),
                modifier = Modifier.semantics { heading() },
            )
        },
        navigationIcon = {
            TextButton(onClick = { onIntent(I.CancelClicked) }) { Text(stringResource(Res.string.editor_cancel)) }
        },
        actions = {
            TextButton(onClick = { onIntent(I.SaveClicked) }, enabled = state.phase == HabitEditorState.Phase.EDITING) {
                Text(stringResource(Res.string.editor_save))
            }
        },
    )
}

@Composable
private fun EditorForm(
    state: HabitEditorState,
    onIntent: (HabitEditorIntent) -> Unit,
    formatTime: (LocalTime) -> String,
    nameFocusRequester: FocusRequester,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FloordayDimens.gutter, vertical = FloordayDimens.spaceL),
        verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceL),
    ) {
        val summary = when {
            state.writeFailed -> Res.string.editor_write_failed
            state.visibleErrors.isNotEmpty() -> Res.string.editor_error_summary
            else -> null
        }
        if (summary != null) {
            Text(
                text = stringResource(summary),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        val nameError = state.error(EditorField.NAME)?.message()
        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(I.NameChanged(it)) },
            label = { Text(stringResource(Res.string.editor_name_label)) },
            placeholder = { Text(stringResource(Res.string.editor_name_placeholder)) },
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocusRequester)
                .semantics { if (nameError != null) error(nameError) },
        )

        SectionTitle(stringResource(Res.string.editor_kind_label))
        KindSelector(selected = state.kind, onSelect = { onIntent(I.KindChanged(it)) })

        if (state.isMedication) {
            val unitError = state.error(EditorField.UNIT)?.message()
            OutlinedTextField(
                value = state.unitLabel,
                onValueChange = { onIntent(I.UnitChanged(it)) },
                label = { Text(stringResource(Res.string.editor_unit_label)) },
                placeholder = { Text(stringResource(Res.string.editor_unit_placeholder)) },
                singleLine = true,
                isError = unitError != null,
                supportingText = unitError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth().semantics { if (unitError != null) error(unitError) },
            )
        }

        SectionTitle(stringResource(if (state.isMedication) Res.string.editor_slots_medication else Res.string.editor_slots_check))
        state.slots.forEachIndexed { index, slot -> SlotEditor(slot, index, state, formatTime, onIntent) }
        if (state.canAddSlot) {
            OutlinedButton(onClick = { onIntent(I.SlotAdded) }) {
                Text(stringResource(if (state.isMedication) Res.string.editor_slot_add_medication else Res.string.editor_slot_add_check))
            }
        }

        DaysSelector(
            days = state.days,
            error = state.error(EditorField.DAYS)?.message(),
            onToggle = { onIntent(I.DayToggled(it)) },
            onEveryDay = { onIntent(I.EveryDaySelected) },
        )

        LabeledSwitch(
            label = stringResource(Res.string.editor_counts_label),
            hint = stringResource(Res.string.editor_counts_hint),
            checked = state.countsForMinimum,
            onCheckedChange = { onIntent(I.CountsForMinimumChanged(it)) },
        )

        if (state.canDelete) {
            OutlinedButton(
                onClick = { onIntent(I.DeleteClicked) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(Res.string.editor_delete)) }
        }
        Spacer(Modifier.height(FloordayDimens.spaceXxl))
    }
}

@Composable
private fun DeleteDialog(name: String, onIntent: (HabitEditorIntent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onIntent(I.DeleteDismissed) },
        title = { Text(stringResource(Res.string.editor_delete_title, name)) },
        text = { Text(stringResource(Res.string.editor_delete_body)) },
        confirmButton = {
            TextButton(
                onClick = { onIntent(I.DeleteConfirmed) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(Res.string.editor_delete_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(I.DeleteDismissed) }) { Text(stringResource(Res.string.editor_cancel)) }
        },
    )
}

private val DEFAULT_PICKER_TIME = LocalTime(8, 0)

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.semantics { heading() })
}

@Composable
private fun KindSelector(selected: HabitKind, onSelect: (HabitKind) -> Unit) {
    val kinds = HabitKind.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        kinds.forEachIndexed { index, kind ->
            SegmentedButton(
                selected = kind == selected,
                onClick = { onSelect(kind) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = kinds.size),
            ) {
                Text(
                    stringResource(
                        when (kind) {
                            HabitKind.CHECK -> Res.string.editor_kind_check
                            HabitKind.MEDICATION -> Res.string.editor_kind_medication
                        },
                    ),
                )
            }
        }
    }
}
