package studio.mialdea.floorday.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/** Entry point used by :shared: gets the ViewModel and draws [HistoryScreen]. */
@Composable
fun HistoryRoute(modifier: Modifier = Modifier, viewModel: HistoryViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(viewModel) {
        viewModel.onIntent(HistoryIntent.Resumed)
        onPauseOrDispose { }
    }

    HistoryScreen(state = state, modifier = modifier)
}
