package studio.mialdea.floorday.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * WCAG 2.x contrast of every pair the components draw: 4.5:1 for text (1.4.3) and 3:1 for
 * non-text (1.4.11). Changing a token means passing this test.
 *
 * Excluded on purpose because they are decorative or carry no information on their own
 * (ADR-0015): ringTrack/surface, outlineVariant/surface, row tile/surface, met fill/surface,
 * seal against the arc at the joint (the halo separates them), arc against the numeral.
 */
class ContrastTest {

    private data class ContrastPair(val name: String, val foreground: Color, val background: Color)

    private infix fun Color.on(background: Color) = this to background

    private fun pairs(vararg entries: Pair<String, Pair<Color, Color>>) =
        entries.map { (name, colors) -> ContrastPair(name, colors.first, colors.second) }

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
    }

    private fun textPairs(s: ColorScheme, x: FloordayColors): List<ContrastPair> {
        val row = s.surfaceContainerLow
        val pressedRow = x.pressedLayer.compositeOver(row)
        return pairs(
            "onSurface sobre surface" to (s.onSurface on s.surface),
            "onSurface sobre la fila" to (s.onSurface on row),
            "onSurface sobre la fila pulsada" to (s.onSurface on pressedRow),
            "onSurface sobre surfaceContainerHighest" to (s.onSurface on s.surfaceContainerHighest),
            "onSurfaceVariant sobre surface" to (s.onSurfaceVariant on s.surface),
            "onSurfaceVariant sobre la fila" to (s.onSurfaceVariant on row),
            "onSurfaceVariant sobre la fila pulsada" to (s.onSurfaceVariant on pressedRow),
            "onSurfaceVariant sobre surfaceContainerHigh" to (s.onSurfaceVariant on s.surfaceContainerHigh),
            "onSurfaceVariant sobre surfaceContainerHighest" to (s.onSurfaceVariant on s.surfaceContainerHighest),
            "onSurfaceVariant sobre surfaceVariant" to (s.onSurfaceVariant on s.surfaceVariant),
            "Hecho en primary sobre la fila" to (s.primary on row),
            "primary sobre surface" to (s.primary on s.surface),
            "onPrimary sobre primary" to (s.onPrimary on s.primary),
            "onPrimaryContainer sobre primaryContainer" to (s.onPrimaryContainer on s.primaryContainer),
            "numero del anillo sobre el relleno de minimo cumplido" to (s.onSurface on x.minimumMetFill),
            "texto de minimo cumplido sobre su relleno" to (x.minimumMetText on x.minimumMetFill),
            "texto de minimo cumplido sobre surface" to (x.minimumMetText on s.surface),
            "secondary sobre surface" to (s.secondary on s.surface),
            "onSecondary sobre secondary" to (s.onSecondary on s.secondary),
            "onSecondaryContainer sobre secondaryContainer" to (s.onSecondaryContainer on s.secondaryContainer),
            "tertiary sobre surface" to (s.tertiary on s.surface),
            "onTertiary sobre tertiary" to (s.onTertiary on s.tertiary),
            "onTertiaryContainer sobre tertiaryContainer" to (s.onTertiaryContainer on s.tertiaryContainer),
            "onSurface sobre freezeContainer" to (s.onSurface on x.freezeContainer),
            "inverseOnSurface sobre inverseSurface" to (s.inverseOnSurface on s.inverseSurface),
            "inversePrimary sobre inverseSurface" to (s.inversePrimary on s.inverseSurface),
            "error sobre surface" to (s.error on s.surface),
            "onError sobre error" to (s.onError on s.error),
            "onErrorContainer sobre errorContainer" to (s.onErrorContainer on s.errorContainer),
            "onBackground sobre background" to (s.onBackground on s.background),
        )
    }

    private fun nonTextPairs(s: ColorScheme, x: FloordayColors): List<ContrastPair> {
        val row = s.surfaceContainerLow
        val pressedRow = x.pressedLayer.compositeOver(row)
        return pairs(
            "borde del check sobre surface" to (x.checkBorder on s.surface),
            "borde del check sobre la fila" to (x.checkBorder on row),
            "borde del check sobre la fila pulsada" to (x.checkBorder on pressedRow),
            "borde del check sobre surfaceContainerHigh" to (x.checkBorder on s.surfaceContainerHigh),
            "check hecho sobre la fila" to (x.checkDoneFill on row),
            "check hecho sobre la fila pulsada" to (x.checkDoneFill on pressedRow),
            "marca del check sobre su relleno" to (x.checkDoneMark on x.checkDoneFill),
            "borde de omitida sobre su relleno" to (x.checkBorder on x.skippedFill),
            "guion de omitida sobre su relleno" to (x.skippedMark on x.skippedFill),
            "glifo de suelo sobre la fila" to (s.onSurfaceVariant on row),
            "outline sobre surface" to (s.outline on s.surface),
            "outline sobre surfaceContainerHigh" to (s.outline on s.surfaceContainerHigh),
            "arco del anillo sobre surface" to (x.ringProgress on s.surface),
            "arco del anillo sobre la pista" to (x.ringProgress on x.ringTrack),
            "arco del anillo sobre el relleno de minimo cumplido" to (x.ringProgress on x.minimumMetFill),
            "marca de paso sobre la pista" to (x.ringTick on x.ringTrack),
            "marca de paso y linea de suelo sobre surface" to (x.ringTick on s.surface),
            "sello sobre su halo" to (x.seal on s.surface),
            "check del sello sobre el sello" to (x.onSeal on x.seal),
            "anillo de foco sobre surface" to (x.focusRing on s.surface),
            "anillo de foco sobre la fila" to (x.focusRing on row),
            "punto de dia no cumplido sobre surface" to (x.missedDay on s.surface),
            "racha sobre surface" to (x.streak on s.surface),
            "comodin sobre freezeContainer" to (x.freeze on x.freezeContainer),
            "comodin sobre la fila" to (x.freeze on row),
            "copo sobre el disco del comodin" to (x.onFreeze on x.freeze),
        )
    }

    private fun assertAtLeast(pairs: List<ContrastPair>, minimum: Float) {
        val failures = pairs.mapNotNull { pair ->
            val ratio = contrast(pair.foreground, pair.background)
            if (ratio < minimum) "${pair.name} = $ratio" else null
        }
        assertTrue(failures.isEmpty(), "Pares por debajo de $minimum a 1 -> $failures")
    }

    @Test
    fun `el texto en claro cumple AA`() =
        assertAtLeast(textPairs(FloordayLightColorScheme, LightFloordayColors), TEXT_MINIMUM)

    @Test
    fun `el texto en oscuro cumple AA`() =
        assertAtLeast(textPairs(FloordayDarkColorScheme, DarkFloordayColors), TEXT_MINIMUM)

    @Test
    fun `lo que no es texto en claro llega a 3 a 1`() =
        assertAtLeast(nonTextPairs(FloordayLightColorScheme, LightFloordayColors), NON_TEXT_MINIMUM)

    @Test
    fun `lo que no es texto en oscuro llega a 3 a 1`() =
        assertAtLeast(nonTextPairs(FloordayDarkColorScheme, DarkFloordayColors), NON_TEXT_MINIMUM)

    private companion object {
        const val TEXT_MINIMUM = 4.5f
        const val NON_TEXT_MINIMUM = 3f
    }
}
