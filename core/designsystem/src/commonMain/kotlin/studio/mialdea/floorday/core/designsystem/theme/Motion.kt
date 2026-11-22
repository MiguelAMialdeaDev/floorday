package studio.mialdea.floorday.core.designsystem.theme

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset

/**
 * Motion tokens. Springs never overshoot except [seal], the one bounce of the app, when the
 * minimum is met. With "Remove animations" (Android) or Reduce Motion (iOS) the duration scale
 * is 0 and every spec snaps to its final, complete static state.
 */
@Suppress("MagicNumber")
object FloordayMotion {
    /** Glyph fill, borders and tints (about 120 ms). */
    val colorFx: SpringSpec<Color> = spring(dampingRatio = 1f, stiffness = 1600f)

    /** Row scale while pressed. */
    val press: SpringSpec<Float> = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMedium)

    /** Check and dash drawing on, and the corner morph from squircle to circle (about 220 ms). */
    val state: SpringSpec<Float> = spring(dampingRatio = 1f, stiffness = 700f)

    /** The check retracts on undo: quicker and plain. */
    val undoDraw: TweenSpec<Float> = tween(durationMillis = 120, easing = LinearOutSlowInEasing)

    /** Tiny glyph pop on check (0.88 to 1). Not a bounce. */
    val glyphPop: SpringSpec<Float> = spring(dampingRatio = 0.6f, stiffness = 1500f)

    /** Ring arms rise like a level (about 600 ms) and keep their velocity on undo. */
    val ringLevel: SpringSpec<Float> = spring(dampingRatio = 1f, stiffness = Spring.StiffnessLow)

    /** Digits slide a third of their height when the count changes. */
    val numeral: SpringSpec<IntOffset> = spring(dampingRatio = 1f, stiffness = 700f)

    /** The only bouncy spec: the seal scales in when the minimum is met. */
    val seal: SpringSpec<Float> = spring(dampingRatio = 0.55f, stiffness = 600f)

    const val SEAL_DELAY_MS = 80L
    const val CROSSFADE_MS = 150
    const val PRESSED_SCALE = 0.98f
    const val GLYPH_POP_FROM = 0.88f
}

/**
 * True when the platform asks for no animations. Used only to skip decorative extras (the pop,
 * the seal delay, the digit slide); the specs already snap on their own.
 * Known gap: it is read once per composition, so a change of the setting while the app runs is
 * picked up on the next launch.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
internal fun rememberReducedMotion(): Boolean {
    val scope = rememberCoroutineScope()
    return remember(scope) { scope.coroutineContext[MotionDurationScale]?.scaleFactor == 0f }
}
