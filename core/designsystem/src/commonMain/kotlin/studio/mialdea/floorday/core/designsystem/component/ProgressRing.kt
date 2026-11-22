package studio.mialdea.floorday.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import studio.mialdea.floorday.core.designsystem.resources.Res
import studio.mialdea.floorday.core.designsystem.resources.ring_content_description
import studio.mialdea.floorday.core.designsystem.resources.ring_minimum_met
import studio.mialdea.floorday.core.designsystem.resources.ring_of
import studio.mialdea.floorday.core.designsystem.resources.ring_of_compact
import studio.mialdea.floorday.core.designsystem.theme.FloordayDimens
import studio.mialdea.floorday.core.designsystem.theme.FloordayMotion
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme
import studio.mialdea.floorday.core.designsystem.theme.LocalReducedMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Hero: the big ring of the day. Compact: the 72 dp ring of the Today header. */
enum class RingVariant { Hero, Compact }

private const val MAX_RING_FONT_SCALE = 1.6f
private const val BOTTOM_ANGLE = 90f
private const val HALF_TURN = 180f
private const val MAX_TICKS = 12
private const val FRACTION_EPSILON = 0.001f
private const val SEAL_CHECK_STROKE_HERO = 2f
private const val SEAL_CHECK_STROKE_COMPACT = 1.5f

/** Ring fraction: 0 when nothing is required (a rest day is told by the screen), capped at 1. */
internal fun ringFraction(done: Int, required: Int): Float =
    if (required <= 0) 0f else done.coerceIn(0, required).toFloat() / required

/**
 * Minimum Day ring. It rises from the floor like a level: two arms grow from 6 o'clock, one per
 * side, with a tick for each required slot still to do. When they meet at 12 o'clock the
 * interior fills, the seal sets on the joint and the caption says the minimum is met: three
 * cues (shape, fill and words), the same with or without animation.
 *
 * It grows with the font scale (up to 1.6x) so the numeral never overflows at 200 %.
 * [stateText] is what TalkBack reads; when null it says "Minimum day: X of N done".
 * [onMinimumMet] runs when the minimum becomes met while the ring is shown (not if it already
 * was): the screen decides the once-per-day haptic.
 */
@Composable
fun ProgressRing(
    done: Int,
    required: Int,
    modifier: Modifier = Modifier,
    stateText: String? = null,
    variant: RingVariant = RingVariant.Hero,
    onMinimumMet: () -> Unit = {},
) {
    val safeRequired = required.coerceAtLeast(0)
    val shownDone = done.coerceIn(0, safeRequired.coerceAtLeast(0))
    val met = safeRequired > 0 && done >= safeRequired
    val hero = variant == RingVariant.Hero
    val colors = FloordayTheme.colors
    val surface = MaterialTheme.colorScheme.surface
    val reduced = LocalReducedMotion.current
    val density = LocalDensity.current
    val fontScale = density.fontScale.coerceIn(1f, MAX_RING_FONT_SCALE)
    val ringSize = (if (hero) FloordayDimens.ringHeroSize else FloordayDimens.ringCompactSize) * fontScale
    val stroke = if (hero) FloordayDimens.ringHeroStroke else FloordayDimens.ringCompactStroke
    val sealSize = if (hero) FloordayDimens.sealHero else FloordayDimens.sealCompact
    val description = stateText ?: stringResource(Res.string.ring_content_description, shownDone, safeRequired)

    val fraction by animateFloatAsState(
        targetValue = ringFraction(done, required),
        animationSpec = FloordayMotion.ringLevel,
        label = "ringLevel",
    )
    val metFill by animateFloatAsState(
        targetValue = if (met) 1f else 0f,
        animationSpec = tween(FloordayMotion.CROSSFADE_MS),
        label = "metFill",
    )
    val sealScale = remember { Animatable(if (met) 1f else 0f) }
    LaunchedEffect(met) {
        if (met) {
            if (!reduced) delay(FloordayMotion.SEAL_DELAY_MS)
            sealScale.animateTo(1f, FloordayMotion.seal)
        } else {
            sealScale.animateTo(0f, FloordayMotion.state)
        }
    }
    val currentOnMinimumMet by rememberUpdatedState(onMinimumMet)
    var wasMet by remember { mutableStateOf(met) }
    LaunchedEffect(met) {
        if (met && !wasMet) currentOnMinimumMet()
        wasMet = met
    }
    val sealPx = with(density) { sealSize.toPx() }
    val haloPx = with(density) { FloordayDimens.sealHalo.toPx() }
    val seal = remember(sealPx) { selloPath(sealPx) }
    val haloSize = sealPx + 2 * haloPx
    val halo = remember(haloSize) { selloPath(haloSize) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .progressSemantics(
                    value = shownDone.toFloat(),
                    valueRange = 0f..safeRequired.coerceAtLeast(1).toFloat(),
                )
                .semantics { stateDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(ringSize)) {
                val sw = stroke.toPx()
                val radius = (size.minDimension - sw) / 2f
                if (metFill > 0f) {
                    drawCircle(
                        color = colors.minimumMetFill,
                        radius = radius - sw / 2f - FloordayDimens.ringMetFillGap.toPx(),
                        alpha = metFill,
                    )
                }
                drawCircle(color = colors.ringTrack, radius = radius, style = Stroke(sw))
                if (hero && safeRequired in 2..MAX_TICKS) drawTicks(radius, safeRequired, fraction, colors.ringTick)
                drawArms(radius, sw, fraction, colors.ringProgress)
                if (sealScale.value > 0f) {
                    val top = Offset(center.x, center.y - radius)
                    scale(sealScale.value, pivot = top) {
                        translate(top.x - haloSize / 2f, top.y - haloSize / 2f) {
                            drawPath(halo, surface)
                        }
                        translate(top.x - sealPx / 2f, top.y - sealPx / 2f) {
                            drawPath(seal, colors.seal)
                            drawSealCheck(sealPx, if (hero) SEAL_CHECK_STROKE_HERO else SEAL_CHECK_STROKE_COMPACT, colors.onSeal)
                        }
                    }
                }
            }
            if (safeRequired > 0) {
                RingCentre(
                    shownDone = shownDone,
                    required = safeRequired,
                    met = met,
                    hero = hero,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
        if (hero) {
            Spacer(Modifier.height(FloordayDimens.floorLineGap))
            Box(
                Modifier
                    .size(FloordayDimens.floorLineWidth, FloordayDimens.floorLineHeight)
                    .background(colors.ringTick, CircleShape),
            )
        }
    }
}

@Composable
private fun RingCentre(shownDone: Int, required: Int, met: Boolean, hero: Boolean, modifier: Modifier) {
    val type = FloordayTheme.type
    val colors = FloordayTheme.colors
    val scheme = MaterialTheme.colorScheme
    if (hero) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            SlotNumeral(value = shownDone, style = type.ringNumeral, color = scheme.onSurface)
            Spacer(Modifier.height(FloordayDimens.spaceXs))
            Text(
                text = if (met) stringResource(Res.string.ring_minimum_met) else stringResource(Res.string.ring_of, required),
                style = if (met) type.ringOf.copy(fontWeight = FontWeight.SemiBold) else type.ringOf,
                color = if (met) colors.minimumMetText else scheme.onSurfaceVariant,
            )
        }
    } else {
        Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
            SlotNumeral(value = shownDone, style = type.ringNumeralCompact, color = scheme.onSurface)
            Text(
                text = stringResource(Res.string.ring_of_compact, required),
                style = type.ringOfCompact,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

/** Two arms from 6 o'clock, one clockwise and one anticlockwise, each sweeping up to half a turn. */
private fun DrawScope.drawArms(radius: Float, strokePx: Float, fraction: Float, color: Color) {
    if (fraction <= 0f) return
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(2 * radius, 2 * radius)
    val sweep = HALF_TURN * fraction.coerceIn(0f, 1f)
    val style = Stroke(width = strokePx, cap = StrokeCap.Round)
    drawArc(color, BOTTOM_ANGLE, sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = style)
    drawArc(color, BOTTOM_ANGLE, -sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = style)
}

/** One short tick per required slot still to do, on both sides, centred on the stroke. */
private fun DrawScope.drawTicks(radius: Float, required: Int, fraction: Float, color: Color) {
    val half = FloordayDimens.ringTickLength.toPx() / 2f
    val width = FloordayDimens.ringTickWidth.toPx()
    for (k in 1 until required) {
        val stepFraction = k.toFloat() / required
        if (stepFraction <= fraction + FRACTION_EPSILON) continue
        val offset = HALF_TURN * stepFraction
        listOf(BOTTOM_ANGLE + offset, BOTTOM_ANGLE - offset).forEach { degrees ->
            val radians = degrees * (PI.toFloat() / HALF_TURN)
            val direction = Offset(cos(radians), sin(radians))
            drawLine(
                color = color,
                start = center + direction * (radius - half),
                end = center + direction * (radius + half),
                strokeWidth = width,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun DrawScope.drawSealCheck(sealPx: Float, strokeDp: Float, color: Color) {
    val path = Path().apply {
        moveTo(sealPx * CHECK_START_X, sealPx * CHECK_START_Y)
        relativeLineTo(sealPx * CHECK_DOWN, sealPx * CHECK_DOWN)
        relativeLineTo(sealPx * CHECK_UP_X, -sealPx * CHECK_UP_Y)
    }
    drawPath(path, color, style = Stroke(width = min(strokeDp.dp.toPx(), sealPx / 6f), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private const val CHECK_START_X = 0.30f
private const val CHECK_START_Y = 0.50f
private const val CHECK_DOWN = 0.13f
private const val CHECK_UP_X = 0.27f
private const val CHECK_UP_Y = 0.30f
