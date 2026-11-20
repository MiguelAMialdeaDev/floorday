package studio.mialdea.floorday.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import studio.mialdea.floorday.feature.history.resources.Res
import studio.mialdea.floorday.feature.history.resources.history_empty
import studio.mialdea.floorday.feature.history.resources.history_title

/** S0 placeholder screen. The real history arrives in S4. */
@Composable
fun HistoryRoute(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.history_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(stringResource(Res.string.history_empty), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
