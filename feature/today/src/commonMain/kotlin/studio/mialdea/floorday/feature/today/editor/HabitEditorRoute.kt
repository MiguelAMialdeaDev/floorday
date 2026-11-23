package studio.mialdea.floorday.feature.today.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import studio.mialdea.floorday.core.designsystem.mvi.CollectEffects
import studio.mialdea.floorday.core.domain.model.HabitId

/**
 * Entry point used by :shared. [habitId] null = new habit. :shared's NavDisplay gives each entry its
 * own ViewModelStore, so editing habit B after A never reuses A's ViewModel.
 */
@Composable
fun HabitEditorRoute(
    habitId: HabitId?,
    onClose: (CloseResult) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitEditorViewModel = koinViewModel { parametersOf(habitId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nameFocusRequester = remember { FocusRequester() }
    // The 12/24 h switch is not a configuration change: read it again when coming back to the app.
    var is24Hour by remember { mutableStateOf(viewModel.is24Hour()) }
    LifecycleResumeEffect(viewModel) {
        is24Hour = viewModel.is24Hour()
        onPauseOrDispose { }
    }

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is HabitEditorEffect.Close -> onClose(effect.result)
            // The other fields carry their error in the summary and on the field itself.
            is HabitEditorEffect.FocusError -> if (effect.field == EditorField.NAME) nameFocusRequester.requestFocus()
        }
    }

    HabitEditorScreen(
        state = state,
        onIntent = viewModel::onIntent,
        formatTime = viewModel::formatTime,
        is24Hour = is24Hour,
        modifier = modifier,
        nameFocusRequester = nameFocusRequester,
    )
}
