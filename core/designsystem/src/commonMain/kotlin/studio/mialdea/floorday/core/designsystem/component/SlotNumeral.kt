package studio.mialdea.floorday.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import studio.mialdea.floorday.core.designsystem.theme.FloordayMotion
import studio.mialdea.floorday.core.designsystem.theme.LocalReducedMotion

private const val SLIDE_FRACTION = 3

/**
 * A number whose digits sit in fixed-width slots, so a face without tabular figures (Fraunces)
 * does not jiggle when the value changes. Each digit slides a third of its height: up when it
 * grows, down when it shrinks. Decorative only: callers clear its semantics and say the value.
 */
@Composable
internal fun SlotNumeral(value: Int, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val reduced = LocalReducedMotion.current
    val slot = remember(style, density) {
        with(density) { (0..9).maxOf { measurer.measure("$it", style).size.width }.toDp() }
    }
    Row(modifier) {
        value.toString().forEachIndexed { index, digit ->
            key(index) {
                AnimatedContent(
                    targetState = digit,
                    transitionSpec = { digitTransition(up = targetState > initialState, reduced = reduced) },
                    label = "digit",
                ) { shown ->
                    Box(Modifier.width(slot), contentAlignment = Alignment.Center) {
                        Text(text = shown.toString(), style = style, color = color)
                    }
                }
            }
        }
    }
}

private fun digitTransition(up: Boolean, reduced: Boolean): ContentTransform {
    if (reduced) return EnterTransition.None togetherWith ExitTransition.None
    val direction = if (up) 1 else -1
    return (slideInVertically(FloordayMotion.numeral) { direction * it / SLIDE_FRACTION } + fadeIn()) togetherWith
        (slideOutVertically(FloordayMotion.numeral) { -direction * it / SLIDE_FRACTION } + fadeOut())
}
