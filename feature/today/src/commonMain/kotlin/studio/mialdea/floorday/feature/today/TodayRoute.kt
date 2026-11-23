package studio.mialdea.floorday.feature.today

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.rememberResourceEnvironment
import org.koin.compose.viewmodel.koinViewModel
import studio.mialdea.floorday.core.designsystem.mvi.CollectEffects
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.feature.today.resources.Res
import studio.mialdea.floorday.feature.today.resources.today_save_error
import studio.mialdea.floorday.feature.today.resources.today_undo_action
import studio.mialdea.floorday.feature.today.resources.today_undo_marked
import studio.mialdea.floorday.feature.today.resources.today_undo_unmarked

private const val UNDO_TIMEOUT_MS = 5_000L

/** Entry point used by :shared: gets the ViewModel, shows its effects and draws [TodayScreen]. */
@Composable
fun TodayRoute(
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    /** null = new habit. */
    onOpenEditor: (HabitId?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val accessibilityManager = LocalAccessibilityManager.current
    // getString without an environment uses the system locale, not the composition's one, so
    // the snackbar could come out in another language than the screen.
    val resources by rememberUpdatedState(rememberResourceEnvironment())
    var snackbarJob by remember { mutableStateOf<Job?>(null) }

    LifecycleResumeEffect(viewModel) {
        viewModel.onIntent(TodayIntent.Resumed)
        onPauseOrDispose { }
    }

    CollectEffects(viewModel.effects) { effect ->
        // A new message replaces the one on screen.
        snackbarJob?.cancel()
        snackbarJob = scope.launch {
            when (effect) {
                is TodayEffect.ShowUndo -> {
                    var result: SnackbarResult? = null
                    try {
                        val message = getString(
                            resources,
                            if (effect.markedDone) Res.string.today_undo_marked else Res.string.today_undo_unmarked,
                            effect.habitName,
                        )
                        // TalkBack and "time to take action" users get the longer time they asked for.
                        val timeout = accessibilityManager?.calculateRecommendedTimeoutMillis(
                            originalTimeoutMillis = UNDO_TIMEOUT_MS,
                            containsText = true,
                            containsControls = true,
                        ) ?: UNDO_TIMEOUT_MS
                        result = withTimeoutOrNull(timeout) {
                            snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = getString(resources, Res.string.today_undo_action),
                                withDismissAction = true,
                                duration = SnackbarDuration.Indefinite,
                            )
                        }
                    } finally {
                        // Also when cut short by another message or by leaving the screen, so the
                        // undo token never stays armed.
                        val undone = result == SnackbarResult.ActionPerformed
                        viewModel.onIntent(if (undone) TodayIntent.Undo(effect.undoId) else TodayIntent.UndoDismissed(effect.undoId))
                    }
                }

                TodayEffect.ShowSaveError -> snackbarHostState.showSnackbar(getString(resources, Res.string.today_save_error))
            }
        }
    }

    TodayScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings,
        onOpenEditor = onOpenEditor,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}
