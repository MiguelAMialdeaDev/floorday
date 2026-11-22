package studio.mialdea.floorday.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

val LocalFloordayColors = staticCompositionLocalOf { LightFloordayColors }

val LocalFloordayType = staticCompositionLocalOf<FloordayType> {
    error("FloordayType is only available inside FloordayTheme")
}

/**
 * Floorday theme. No dynamic colour on purpose: it would erase the identity (ADR-0015).
 * Standard motion, not expressive: stock components must not bounce.
 * [darkTheme] follows the system; Settings will force it through this parameter.
 */
@Composable
fun FloordayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val fonts = rememberFloordayFonts()
    val typography = remember(fonts) { floordayTypography(fonts) }
    val type = remember(fonts) { floordayType(fonts) }
    CompositionLocalProvider(
        LocalFloordayColors provides if (darkTheme) DarkFloordayColors else LightFloordayColors,
        LocalFloordayType provides type,
        LocalReducedMotion provides rememberReducedMotion(),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) FloordayDarkColorScheme else FloordayLightColorScheme,
            motionScheme = MotionScheme.standard(),
            shapes = FloordayShapes,
            typography = typography,
            content = content,
        )
    }
}

/** Floorday tokens that MaterialTheme does not carry. */
object FloordayTheme {
    val colors: FloordayColors
        @Composable @ReadOnlyComposable
        get() = LocalFloordayColors.current

    val type: FloordayType
        @Composable @ReadOnlyComposable
        get() = LocalFloordayType.current
}
