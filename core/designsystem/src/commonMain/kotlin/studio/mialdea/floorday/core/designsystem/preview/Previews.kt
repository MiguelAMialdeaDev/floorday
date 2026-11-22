package studio.mialdea.floorday.core.designsystem.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import studio.mialdea.floorday.core.designsystem.component.CheckRow
import studio.mialdea.floorday.core.designsystem.component.CheckState
import studio.mialdea.floorday.core.designsystem.component.ProgressRing
import studio.mialdea.floorday.core.designsystem.component.RingVariant
import studio.mialdea.floorday.core.designsystem.theme.FloordayDimens
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme

// Generic sample texts on purpose: no real medication or names (the repo is public).

@Composable
private fun ComponentsSample() {
    Surface {
        Column(
            modifier = Modifier.padding(FloordayDimens.gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceXxl),
        ) {
            Text(
                text = "Hoy",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.align(Alignment.Start),
            )
            ProgressRing(done = 2, required = 4)
            Column(verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceXs)) {
                Text(
                    text = "MAÑANA",
                    style = FloordayTheme.type.eyebrow,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = FloordayDimens.spaceXs),
                )
                CheckRow(
                    label = "Pastilla mañana, ½",
                    supportingText = "08:00",
                    doneTime = "08:42",
                    state = CheckState.Done,
                    partOfMinimum = true,
                    onToggle = {},
                )
                CheckRow(label = "Vaso de agua", supportingText = "08:00", state = CheckState.Pending, partOfMinimum = true, onToggle = {})
                CheckRow(label = "Paseo corto", supportingText = "Omitida hoy", state = CheckState.Skipped, onToggle = {})
                CheckRow(label = "Leer", supportingText = "Disponible a las 20:00", state = CheckState.Pending, enabled = false, onToggle = {})
            }
        }
    }
}

@Preview(name = "Componentes claro", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ComponentsLightPreview() {
    FloordayTheme(darkTheme = false) { ComponentsSample() }
}

@Preview(name = "Componentes oscuro", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ComponentsDarkPreview() {
    FloordayTheme(darkTheme = true) { ComponentsSample() }
}

@Preview(name = "Componentes fuente 200 %", showBackground = true, widthDp = 360, heightDp = 1400, fontScale = 2f)
@Composable
private fun ComponentsFontScalePreview() {
    FloordayTheme(darkTheme = false) { ComponentsSample() }
}

@Composable
private fun RingStates() {
    Surface {
        Column(
            modifier = Modifier.padding(FloordayDimens.gutter),
            verticalArrangement = Arrangement.spacedBy(FloordayDimens.spaceXl),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceL)) {
                ProgressRing(done = 0, required = 4)
                ProgressRing(done = 4, required = 4)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceL)) {
                ProgressRing(done = 1, required = 3, variant = RingVariant.Compact)
                ProgressRing(done = 3, required = 3, variant = RingVariant.Compact)
            }
        }
    }
}

@Preview(name = "Anillo vacío, cumplido y compacto (claro)", showBackground = true, widthDp = 420)
@Composable
private fun RingStatesLightPreview() {
    FloordayTheme(darkTheme = false) { RingStates() }
}

@Preview(name = "Anillo vacío, cumplido y compacto (oscuro)", showBackground = true, widthDp = 420)
@Composable
private fun RingStatesDarkPreview() {
    FloordayTheme(darkTheme = true) { RingStates() }
}
