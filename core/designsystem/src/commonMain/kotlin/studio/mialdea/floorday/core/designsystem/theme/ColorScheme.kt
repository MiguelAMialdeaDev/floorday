package studio.mialdea.floorday.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

private const val PRESSED_ALPHA_LIGHT = 0.08f
private const val PRESSED_ALPHA_DARK = 0.10f

internal val FloordayLightColorScheme: ColorScheme = lightColorScheme(
    primary = Palette.Pino700,
    onPrimary = Palette.BlancoCalido,
    primaryContainer = Palette.Pino100,
    onPrimaryContainer = Palette.Pino950,
    inversePrimary = Palette.Pino200,
    secondary = Palette.Barro600,
    onSecondary = Palette.BlancoCalido,
    secondaryContainer = Palette.Barro100,
    onSecondaryContainer = Palette.Barro950,
    tertiary = Palette.Pizarra600,
    onTertiary = Palette.BlancoCalido,
    tertiaryContainer = Palette.Pizarra100,
    onTertiaryContainer = Palette.Pizarra950,
    background = Palette.Hueso,
    onBackground = Palette.Tinta,
    surface = Palette.Hueso,
    onSurface = Palette.Tinta,
    surfaceVariant = Palette.HuesoVariant,
    onSurfaceVariant = Palette.TintaSuave,
    surfaceTint = Palette.Pino700,
    inverseSurface = Palette.TintaInversa,
    inverseOnSurface = Palette.HuesoInverso,
    error = Palette.Error,
    onError = Palette.BlancoCalido,
    errorContainer = Palette.ErrorContainer,
    onErrorContainer = Palette.OnErrorContainer,
    outline = Palette.Piedra,
    // Decorative only: never the boundary of something you can touch.
    outlineVariant = Palette.Arena,
    scrim = Palette.Scrim,
    surfaceBright = Palette.HuesoBright,
    surfaceDim = Palette.HuesoDim,
    surfaceContainerLowest = Palette.BlancoCalido,
    surfaceContainerLow = Palette.Hueso100,
    surfaceContainer = Palette.Hueso200,
    surfaceContainerHigh = Palette.Hueso300,
    surfaceContainerHighest = Palette.Hueso400,
)

internal val FloordayDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Palette.Pino200,
    onPrimary = Palette.Pino900,
    primaryContainer = Palette.Pino600,
    onPrimaryContainer = Palette.Pino50,
    inversePrimary = Palette.Pino700,
    secondary = Palette.Barro200,
    onSecondary = Palette.Barro900,
    secondaryContainer = Palette.Barro700,
    onSecondaryContainer = Palette.Barro50,
    tertiary = Palette.Pizarra200,
    onTertiary = Palette.Pizarra900,
    tertiaryContainer = Palette.Pizarra700,
    onTertiaryContainer = Palette.Pizarra50,
    background = Palette.Noche,
    onBackground = Palette.NocheTexto,
    surface = Palette.Noche,
    onSurface = Palette.NocheTexto,
    surfaceVariant = Palette.NocheVariant,
    onSurfaceVariant = Palette.NocheTextoSuave,
    surfaceTint = Palette.Pino200,
    inverseSurface = Palette.NocheTexto,
    inverseOnSurface = Palette.TintaInversa,
    error = Palette.ErrorDark,
    onError = Palette.OnErrorDark,
    errorContainer = Palette.ErrorContainerDark,
    onErrorContainer = Palette.OnErrorContainerDark,
    outline = Palette.NocheOutline,
    outlineVariant = Palette.NocheOutlineVariant,
    scrim = Palette.Scrim,
    surfaceBright = Palette.NocheBright,
    surfaceDim = Palette.Noche,
    surfaceContainerLowest = Palette.NocheLowest,
    surfaceContainerLow = Palette.NocheLow,
    surfaceContainer = Palette.Noche200,
    surfaceContainerHigh = Palette.Noche300,
    surfaceContainerHighest = Palette.Noche400,
)

/**
 * Floorday roles that Material 3 has no slot for. Strict meaning of colour (ADR-0015):
 * Pino is done or the minimum, Barro the streak, Pizarra a freeze, a neutral outline is
 * pending or missed, and red is never used for a day or a dose.
 */
@Immutable
data class FloordayColors(
    /** Ring arms. A lighter Pino than the numeral so both read as separate things. */
    val ringProgress: Color,
    /** Full ring track. Decorative: progress is also told by the numeral and the semantics. */
    val ringTrack: Color,
    /** One tick per required slot still to do, plus the floor line under the ring. */
    val ringTick: Color,
    /** Ring interior once the minimum is met. */
    val minimumMetFill: Color,
    /** "Minimum met" caption. */
    val minimumMetText: Color,
    /** The scalloped seal at 12 o'clock once the minimum is met. */
    val seal: Color,
    val onSeal: Color,
    /** Border of the pending and skipped check glyph. */
    val checkBorder: Color,
    val checkDoneFill: Color,
    val checkDoneMark: Color,
    val skippedFill: Color,
    val skippedMark: Color,
    /** Our own press layer on CheckRow (there is no ripple). */
    val pressedLayer: Color,
    /** Keyboard and Switch Access focus outline. */
    val focusRing: Color,
    /** Hollow dot for a missed day in the calendar (S4). Never red, never a cross. */
    val missedDay: Color,
    val streak: Color,
    val streakContainer: Color,
    val freeze: Color,
    val onFreeze: Color,
    val freezeContainer: Color,
)

internal val LightFloordayColors = FloordayColors(
    ringProgress = Palette.Pino500,
    ringTrack = Palette.RingTrackLight,
    ringTick = Palette.Piedra,
    minimumMetFill = Palette.Pino100,
    minimumMetText = Palette.Pino800,
    seal = Palette.Pino700,
    onSeal = Palette.BlancoCalido,
    checkBorder = Palette.Piedra,
    checkDoneFill = Palette.Pino700,
    checkDoneMark = Palette.BlancoCalido,
    skippedFill = Palette.Hueso300,
    skippedMark = Palette.TintaSuave,
    pressedLayer = Palette.Tinta.copy(alpha = PRESSED_ALPHA_LIGHT),
    focusRing = Palette.Pino500,
    missedDay = Palette.Piedra,
    streak = Palette.Barro600,
    streakContainer = Palette.Barro100,
    freeze = Palette.Pizarra600,
    onFreeze = Palette.BlancoCalido,
    freezeContainer = Palette.Pizarra100,
)

internal val DarkFloordayColors = FloordayColors(
    ringProgress = Palette.Pino300,
    ringTrack = Palette.RingTrackDark,
    ringTick = Palette.NocheOutline,
    minimumMetFill = Palette.Pino900,
    minimumMetText = Palette.Pino200,
    seal = Palette.Pino200,
    onSeal = Palette.Pino900,
    checkBorder = Palette.NocheOutline,
    checkDoneFill = Palette.Pino200,
    checkDoneMark = Palette.Pino900,
    skippedFill = Palette.Noche300,
    skippedMark = Palette.NocheTextoSuave,
    pressedLayer = Palette.NocheTexto.copy(alpha = PRESSED_ALPHA_DARK),
    focusRing = Palette.Pino300,
    missedDay = Palette.NocheOutline,
    streak = Palette.Barro200,
    streakContainer = Palette.Barro700,
    freeze = Palette.Pizarra200,
    onFreeze = Palette.Pizarra900,
    freezeContainer = Palette.Pizarra700,
)
