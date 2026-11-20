package studio.mialdea.floorday.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme
import studio.mialdea.floorday.feature.today.resources.Res
import studio.mialdea.floorday.feature.today.resources.today_hello
import studio.mialdea.floorday.feature.today.resources.today_open_history
import studio.mialdea.floorday.feature.today.resources.today_open_settings
import studio.mialdea.floorday.feature.today.resources.today_title

/** Punto de entrada que usa :shared. En la issue "Pantalla Hoy" recibirá su ViewModel. */
@Composable
fun TodayRoute(
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TodayScreen(onOpenHistory = onOpenHistory, onOpenSettings = onOpenSettings, modifier = modifier)
}

@Composable
internal fun TodayScreen(
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.today_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(Res.string.today_hello),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onOpenHistory) {
                Text(stringResource(Res.string.today_open_history))
            }
            OutlinedButton(onClick = onOpenSettings) {
                Text(stringResource(Res.string.today_open_settings))
            }
        }
    }
}

@Preview
@Composable
private fun TodayScreenPreview() {
    FloordayTheme { TodayScreen(onOpenHistory = {}, onOpenSettings = {}) }
}
