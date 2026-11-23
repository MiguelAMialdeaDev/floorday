package studio.mialdea.floorday.feature.today.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import studio.mialdea.floorday.core.designsystem.theme.FloordayDimens
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitSlot
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.feature.today.quarterText
import studio.mialdea.floorday.feature.today.resources.Res
import studio.mialdea.floorday.feature.today.resources.day_full_friday
import studio.mialdea.floorday.feature.today.resources.day_full_monday
import studio.mialdea.floorday.feature.today.resources.day_full_saturday
import studio.mialdea.floorday.feature.today.resources.day_full_sunday
import studio.mialdea.floorday.feature.today.resources.day_full_thursday
import studio.mialdea.floorday.feature.today.resources.day_full_tuesday
import studio.mialdea.floorday.feature.today.resources.day_full_wednesday
import studio.mialdea.floorday.feature.today.resources.day_short_friday
import studio.mialdea.floorday.feature.today.resources.day_short_monday
import studio.mialdea.floorday.feature.today.resources.day_short_saturday
import studio.mialdea.floorday.feature.today.resources.day_short_sunday
import studio.mialdea.floorday.feature.today.resources.day_short_thursday
import studio.mialdea.floorday.feature.today.resources.day_short_tuesday
import studio.mialdea.floorday.feature.today.resources.day_short_wednesday
import studio.mialdea.floorday.feature.today.resources.editor_amount_decrease
import studio.mialdea.floorday.feature.today.resources.editor_amount_increase
import studio.mialdea.floorday.feature.today.resources.editor_amount_label
import studio.mialdea.floorday.feature.today.resources.editor_cancel
import studio.mialdea.floorday.feature.today.resources.editor_day_off
import studio.mialdea.floorday.feature.today.resources.editor_day_on
import studio.mialdea.floorday.feature.today.resources.editor_days_every
import studio.mialdea.floorday.feature.today.resources.editor_days_label
import studio.mialdea.floorday.feature.today.resources.editor_error_duplicate_time
import studio.mialdea.floorday.feature.today.resources.editor_error_name_blank
import studio.mialdea.floorday.feature.today.resources.editor_error_needs_time
import studio.mialdea.floorday.feature.today.resources.editor_error_no_days
import studio.mialdea.floorday.feature.today.resources.editor_error_too_long
import studio.mialdea.floorday.feature.today.resources.editor_reminder_gentle
import studio.mialdea.floorday.feature.today.resources.editor_reminder_insistent
import studio.mialdea.floorday.feature.today.resources.editor_reminder_label
import studio.mialdea.floorday.feature.today.resources.editor_reminder_none
import studio.mialdea.floorday.feature.today.resources.editor_slot_default_check
import studio.mialdea.floorday.feature.today.resources.editor_slot_default_medication
import studio.mialdea.floorday.feature.today.resources.editor_slot_label
import studio.mialdea.floorday.feature.today.resources.editor_slot_remove
import studio.mialdea.floorday.feature.today.resources.editor_time_change
import studio.mialdea.floorday.feature.today.resources.editor_time_clear
import studio.mialdea.floorday.feature.today.resources.editor_time_none
import studio.mialdea.floorday.feature.today.resources.editor_time_picker_ok
import studio.mialdea.floorday.feature.today.resources.editor_time_picker_title
// The alias hides the original name in this file, so both imports are needed.
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent
import studio.mialdea.floorday.feature.today.editor.HabitEditorIntent as I

@Composable
internal fun EditorError.message(): String = when (this) {
    EditorError.NameBlank -> stringResource(Res.string.editor_error_name_blank)
    EditorError.NameTooLong -> stringResource(Res.string.editor_error_too_long, Habit.NAME_MAX_LENGTH)
    EditorError.UnitTooLong -> stringResource(Res.string.editor_error_too_long, Habit.UNIT_LABEL_MAX_LENGTH)
    is EditorError.LabelTooLong -> stringResource(Res.string.editor_error_too_long, HabitSlot.LABEL_MAX_LENGTH)
    is EditorError.DuplicateTime -> stringResource(Res.string.editor_error_duplicate_time)
    is EditorError.ReminderNeedsTime -> stringResource(Res.string.editor_error_needs_time)
    EditorError.NoDays -> stringResource(Res.string.editor_error_no_days)
}

/** One slot: label, time, amount (medication) and reminder. Red border and messages if it has errors. */
@Composable
internal fun SlotEditor(
    slot: SlotDraft,
    index: Int,
    state: HabitEditorState,
    formatTime: (LocalTime) -> String,
    onIntent: (HabitEditorIntent) -> Unit,
) {
    val defaultName = stringResource(
        if (state.isMedication) Res.string.editor_slot_default_medication else Res.string.editor_slot_default_check,
        index + 1,
    )
    val errors = state.errorsFor(slot.key).map { it.message() }
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = if (errors.isEmpty()) {
            CardDefaults.outlinedCardBorder()
        } else {
            BorderStroke(FloordayDimens.focusRingWidth, MaterialTheme.colorScheme.error)
        },
    ) {
        Column(Modifier.padding(FloordayDimens.spaceL), verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS)) {
            OutlinedTextField(
                value = slot.label,
                onValueChange = { onIntent(I.LabelChanged(slot.key, it)) },
                label = { Text(stringResource(Res.string.editor_slot_label)) },
                placeholder = { Text(defaultName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TimeRow(slot, formatTime, onIntent)
            if (state.isMedication) {
                AmountStepper(
                    amount = slot.amount,
                    unitLabel = state.unitLabel.trim(),
                    onDecrease = { onIntent(I.AmountDecreased(slot.key)) },
                    onIncrease = { onIntent(I.AmountIncreased(slot.key)) },
                )
            }
            Text(stringResource(Res.string.editor_reminder_label), style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                state.reminderOptions.forEachIndexed { i, mode ->
                    SegmentedButton(
                        selected = slot.reminder == mode,
                        onClick = { onIntent(I.ReminderChanged(slot.key, mode)) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = state.reminderOptions.size),
                        // Without the check icon the three labels fit at 360 dp and large font (spec, trap 17).
                        icon = {},
                    ) { Text(stringResource(mode.label())) }
                }
            }
            errors.forEach { ErrorText(it) }
            if (state.canRemoveSlot) {
                TextButton(onClick = { onIntent(I.SlotRemoved(slot.key)) }) {
                    Text(stringResource(Res.string.editor_slot_remove, slot.label.trim().ifEmpty { defaultName }))
                }
            }
        }
    }
}

@Composable
private fun TimeRow(slot: SlotDraft, formatTime: (LocalTime) -> String, onIntent: (HabitEditorIntent) -> Unit) {
    val time = slot.time
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS)) {
        if (time == null) {
            FilledTonalButton(onClick = { onIntent(I.TimePickerOpened(slot.key)) }) {
                Text(stringResource(Res.string.editor_time_none))
            }
        } else {
            val text = formatTime(time)
            val description = stringResource(Res.string.editor_time_change, text)
            FilledTonalButton(
                onClick = { onIntent(I.TimePickerOpened(slot.key)) },
                modifier = Modifier.semantics { contentDescription = description },
            ) { Text(text, style = MaterialTheme.typography.titleMedium) }
            TextButton(onClick = { onIntent(I.TimeCleared(slot.key)) }) {
                Text(stringResource(Res.string.editor_time_clear))
            }
        }
    }
}

/** − amount + in quarters. The glyphs are hidden from TalkBack, which reads the button labels instead. */
@Composable
private fun AmountStepper(amount: Quarters, unitLabel: String, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    val amountText = if (unitLabel.isEmpty()) quarterText(amount) else "${quarterText(amount)} $unitLabel"
    val amountLabel = stringResource(Res.string.editor_amount_label)
    val decrease = stringResource(Res.string.editor_amount_decrease)
    val increase = stringResource(Res.string.editor_amount_increase)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS)) {
        Text(amountLabel, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        FilledTonalIconButton(
            onClick = onDecrease,
            enabled = amount > Quarters.EDITOR_CHOICES.first(),
            modifier = Modifier.semantics { contentDescription = decrease },
        ) { Text("−", modifier = Modifier.clearAndSetSemantics {}) }
        Text(
            text = amountText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .widthIn(min = FloordayDimens.minTouchTarget)
                .semantics {
                    contentDescription = "$amountLabel: $amountText"
                    liveRegion = LiveRegionMode.Polite
                },
        )
        FilledTonalIconButton(
            onClick = onIncrease,
            enabled = amount < Quarters.EDITOR_CHOICES.last(),
            modifier = Modifier.semantics { contentDescription = increase },
        ) { Text("+", modifier = Modifier.clearAndSetSemantics {}) }
    }
}

/** Monday to Sunday. Each day is a checkbox read as "Monday, Active". */
@Composable
internal fun DaysSelector(days: DaysOfWeek, error: String?, onToggle: (DayOfWeek) -> Unit, onEveryDay: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceS)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.editor_days_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            if (days != DaysOfWeek.EVERY_DAY) {
                TextButton(onClick = onEveryDay) { Text(stringResource(Res.string.editor_days_every)) }
            }
        }
        val on = stringResource(Res.string.editor_day_on)
        val off = stringResource(Res.string.editor_day_off)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceXs)) {
            DayOfWeek.entries.forEach { day ->
                val active = day in days
                val fullName = stringResource(day.fullName())
                val colors = MaterialTheme.colorScheme
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = FloordayDimens.minTouchTarget)
                        .clip(CircleShape)
                        .then(
                            if (active) {
                                Modifier.background(colors.primary, CircleShape)
                            } else {
                                Modifier.border(FloordayDimens.checkGlyphBorder, colors.outline, CircleShape)
                            },
                        )
                        .toggleable(value = active, role = Role.Checkbox, onValueChange = { onToggle(day) })
                        .clearAndSetSemantics {
                            contentDescription = fullName
                            stateDescription = if (active) on else off
                        },
                ) {
                    // Filled and bold when active: the state never depends on colour alone.
                    Text(
                        text = stringResource(day.shortName()),
                        color = if (active) colors.onPrimary else colors.onSurface,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
        if (error != null) ErrorText(error)
    }
}

/** The whole row toggles, read by TalkBack as a switch. */
@Composable
internal fun LabeledSwitch(label: String, hint: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FloordayDimens.minTouchTarget)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
internal fun SlotTimePickerDialog(initial: LocalTime, is24Hour: Boolean, onConfirm: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    // is24Hour is passed explicitly: the picker's own default is internal and could disagree with the row text.
    val pickerState = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = is24Hour)
    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.editor_time_picker_title)) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(Res.string.editor_time_picker_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.editor_cancel)) }
        },
    ) {
        TimePicker(state = pickerState)
    }
}

@Composable
internal fun ErrorText(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

private fun ReminderMode.label(): StringResource = when (this) {
    ReminderMode.NONE -> Res.string.editor_reminder_none
    ReminderMode.GENTLE -> Res.string.editor_reminder_gentle
    ReminderMode.INSISTENT -> Res.string.editor_reminder_insistent
}

private fun DayOfWeek.shortName(): StringResource = when (this) {
    DayOfWeek.MONDAY -> Res.string.day_short_monday
    DayOfWeek.TUESDAY -> Res.string.day_short_tuesday
    DayOfWeek.WEDNESDAY -> Res.string.day_short_wednesday
    DayOfWeek.THURSDAY -> Res.string.day_short_thursday
    DayOfWeek.FRIDAY -> Res.string.day_short_friday
    DayOfWeek.SATURDAY -> Res.string.day_short_saturday
    DayOfWeek.SUNDAY -> Res.string.day_short_sunday
}

private fun DayOfWeek.fullName(): StringResource = when (this) {
    DayOfWeek.MONDAY -> Res.string.day_full_monday
    DayOfWeek.TUESDAY -> Res.string.day_full_tuesday
    DayOfWeek.WEDNESDAY -> Res.string.day_full_wednesday
    DayOfWeek.THURSDAY -> Res.string.day_full_thursday
    DayOfWeek.FRIDAY -> Res.string.day_full_friday
    DayOfWeek.SATURDAY -> Res.string.day_full_saturday
    DayOfWeek.SUNDAY -> Res.string.day_full_sunday
}
