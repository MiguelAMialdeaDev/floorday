package studio.mialdea.floorday.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import studio.mialdea.floorday.core.designsystem.resources.Res
import studio.mialdea.floorday.core.designsystem.resources.atkinson_next_bold
import studio.mialdea.floorday.core.designsystem.resources.atkinson_next_medium
import studio.mialdea.floorday.core.designsystem.resources.atkinson_next_regular
import studio.mialdea.floorday.core.designsystem.resources.atkinson_next_semibold
import studio.mialdea.floorday.core.designsystem.resources.fraunces_soft_semibold

/**
 * Two OFL faces with a strict boundary (ADR-0015):
 * - Atkinson Hyperlegible Next for everything below 28sp, medication names and times included.
 *   It is built for low vision and has tabular figures.
 * - Fraunces 72pt Soft SemiBold for display and headline sizes and the ring numeral only.
 *   It has no tabular figures: numbers that change use SlotNumeral.
 * Static weights only: Font(resource, weight) does not expose variation settings in CMP.
 */
@Immutable
data class FloordayFonts(val text: FontFamily, val display: FontFamily)

@Composable
internal fun rememberFloordayFonts(): FloordayFonts {
    val text = FontFamily(
        Font(Res.font.atkinson_next_regular, FontWeight.Normal),
        Font(Res.font.atkinson_next_medium, FontWeight.Medium),
        Font(Res.font.atkinson_next_semibold, FontWeight.SemiBold),
        Font(Res.font.atkinson_next_bold, FontWeight.Bold),
    )
    val display = FontFamily(Font(Res.font.fraunces_soft_semibold, FontWeight.SemiBold))
    return remember(text, display) { FloordayFonts(text = text, display = display) }
}

private fun style(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    tracking: TextUnit = 0.em,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking,
)

/** Tracking is in em so it scales with the font size. bodyLarge and bodyMedium are one step above M3. */
@Suppress("MagicNumber")
internal fun floordayTypography(fonts: FloordayFonts): Typography {
    val display = fonts.display
    val text = fonts.text
    return Typography(
        displayLarge = style(display, FontWeight.SemiBold, 57, 64, (-0.02).em),
        displayMedium = style(display, FontWeight.SemiBold, 45, 52, (-0.02).em),
        displaySmall = style(display, FontWeight.SemiBold, 36, 44, (-0.015).em),
        headlineLarge = style(display, FontWeight.SemiBold, 32, 40, (-0.01).em),
        headlineMedium = style(display, FontWeight.SemiBold, 28, 36, (-0.01).em),
        headlineSmall = style(text, FontWeight.SemiBold, 24, 32, (-0.01).em),
        titleLarge = style(text, FontWeight.SemiBold, 22, 28, (-0.005).em),
        titleMedium = style(text, FontWeight.SemiBold, 16, 24),
        titleSmall = style(text, FontWeight.Medium, 14, 20, 0.005.em),
        bodyLarge = style(text, FontWeight.Normal, 17, 24),
        bodyMedium = style(text, FontWeight.Normal, 15, 22, 0.005.em),
        bodySmall = style(text, FontWeight.Normal, 13, 18, 0.01.em),
        labelLarge = style(text, FontWeight.SemiBold, 14, 20, 0.01.em),
        labelMedium = style(text, FontWeight.Medium, 12, 16, 0.04.em),
        labelSmall = style(text, FontWeight.Medium, 11, 16, 0.05.em),
    )
}

/** Floorday text roles that the M3 type scale does not have. Colours are set where they are used. */
@Immutable
data class FloordayType(
    /** Hero ring count (176 dp ring). Fraunces, line height trimmed so it centres optically. */
    val ringNumeral: TextStyle,
    /** Count of the 72 dp ring in the Today header. */
    val ringNumeralCompact: TextStyle,
    /** "of 4" under the hero numeral, and the "Minimum met" caption. */
    val ringOf: TextStyle,
    /** "/4" next to the compact numeral. */
    val ringOfCompact: TextStyle,
    /** Times: "08:00", "Done 08:42". Tabular figures and case-sensitive colons. */
    val time: TextStyle,
    /** Short group headers ("MORNING"). The caller uppercases; never for medication names. */
    val eyebrow: TextStyle,
)

@Suppress("MagicNumber")
internal fun floordayType(fonts: FloordayFonts): FloordayType {
    val trimmed = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    )
    val time = style(fonts.text, FontWeight.Medium, 13, 16, 0.04.em).copy(fontFeatureSettings = "tnum, case")
    return FloordayType(
        ringNumeral = style(fonts.display, FontWeight.SemiBold, 56, 56, (-0.02).em).copy(lineHeightStyle = trimmed),
        ringNumeralCompact = style(fonts.display, FontWeight.SemiBold, 26, 26).copy(lineHeightStyle = trimmed),
        ringOf = style(fonts.text, FontWeight.Medium, 15, 20).tabularNumbers(),
        ringOfCompact = style(fonts.text, FontWeight.Medium, 15, 20).tabularNumbers(),
        time = time,
        eyebrow = time.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
    )
}

/** Fixed-width figures for Atkinson counters and times. Do not use it with Fraunces (no tnum). */
fun TextStyle.tabularNumbers(): TextStyle = copy(fontFeatureSettings = "tnum")
