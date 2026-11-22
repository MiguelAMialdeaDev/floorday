package studio.mialdea.floorday.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import studio.mialdea.floorday.core.designsystem.icon.FloordayIcons
import studio.mialdea.floorday.core.designsystem.resources.Res
import studio.mialdea.floorday.core.designsystem.resources.check_action_mark
import studio.mialdea.floorday.core.designsystem.resources.check_action_undo
import studio.mialdea.floorday.core.designsystem.resources.check_done_label
import studio.mialdea.floorday.core.designsystem.resources.check_part_of_minimum
import studio.mialdea.floorday.core.designsystem.resources.check_state_done
import studio.mialdea.floorday.core.designsystem.resources.check_state_done_at
import studio.mialdea.floorday.core.designsystem.resources.check_state_pending
import studio.mialdea.floorday.core.designsystem.resources.check_state_skipped
import studio.mialdea.floorday.core.designsystem.theme.FloordayDimens
import studio.mialdea.floorday.core.designsystem.theme.FloordayMotion
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme
import studio.mialdea.floorday.core.designsystem.theme.LocalReducedMotion
import studio.mialdea.floorday.core.designsystem.theme.concentric

enum class CheckState { Pending, Done, Skipped }

/** Extra action for TalkBack and Switch Access, for example "skip on purpose". */
@Immutable
data class CheckRowAction(val label: String, val onClick: () -> Unit)

private const val DISABLED_ALPHA = 0.38f
private const val CORNER_PENDING = 30f
private const val CORNER_DONE = 50f

/**
 * Row of the Minimum Day. The whole row is the touch target (56 dp minimum height, it grows
 * with the font). A tap toggles; the optional long press opens "skip" (ADR-0015: it is why this
 * uses combinedClickable with Role.Checkbox instead of toggleable).
 *
 * State is told by shape and words, never by colour alone: an empty squircle when pending, a
 * circle with a check when done, a dash when skipped, plus stateDescription. Done keeps its ink
 * (no strikethrough) and shows "Done 08:42" when [doneTime] is given.
 *
 * There is no ripple: the row scales to 0.98 and shows its own press layer. Haptics fire only
 * when an action is committed and never use the reject pattern.
 */
@Composable
fun CheckRow(
    label: String,
    state: CheckState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    doneTime: String? = null,
    partOfMinimum: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    longClickLabel: String? = null,
    extraActions: List<CheckRowAction> = emptyList(),
    enabled: Boolean = true,
    haptics: Boolean = true,
) {
    val colors = FloordayTheme.colors
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) FloordayMotion.PRESSED_SCALE else 1f,
        animationSpec = FloordayMotion.press,
        label = "rowScale",
    )

    val stateText = when {
        state == CheckState.Done && doneTime != null -> stringResource(Res.string.check_state_done_at, doneTime)
        state == CheckState.Done -> stringResource(Res.string.check_state_done)
        state == CheckState.Skipped -> stringResource(Res.string.check_state_skipped)
        else -> stringResource(Res.string.check_state_pending)
    }
    val toggleLabel = stringResource(
        if (state == CheckState.Done) Res.string.check_action_undo else Res.string.check_action_mark,
    )
    val tileShape = MaterialTheme.shapes.large
    val pressedRadius = concentric(FloordayDimens.spaceL, FloordayDimens.pressedInset)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = FloordayDimens.checkRowMinHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(scheme.surfaceContainerLow, tileShape)
            .then(
                if (focused) Modifier.border(FloordayDimens.focusRingWidth, colors.focusRing, tileShape) else Modifier,
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClickLabel = toggleLabel,
                role = Role.Checkbox,
                onLongClickLabel = longClickLabel,
                onLongClick = onLongClick,
                hapticFeedbackEnabled = haptics,
                onClick = {
                    if (haptics) {
                        haptic.performHapticFeedback(
                            if (state == CheckState.Done) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn,
                        )
                    }
                    onToggle()
                },
            )
            .semantics(mergeDescendants = true) {
                toggleableState = if (state == CheckState.Done) ToggleableState.On else ToggleableState.Off
                stateDescription = stateText
                if (enabled && extraActions.isNotEmpty()) {
                    customActions = extraActions.map { action ->
                        CustomAccessibilityAction(action.label) {
                            action.onClick()
                            true
                        }
                    }
                }
            }
            .drawBehind {
                if (pressed && enabled) {
                    // Inset press layer whose radius stays concentric with the 16 dp tile.
                    val inset = FloordayDimens.pressedInset.toPx()
                    drawRoundRect(
                        color = colors.pressedLayer,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - 2 * inset, size.height - 2 * inset),
                        cornerRadius = CornerRadius(pressedRadius.toPx()),
                    )
                }
            }
            .padding(horizontal = FloordayDimens.spaceL, vertical = FloordayDimens.spaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceL),
    ) {
        Box(
            modifier = Modifier
                .size(FloordayDimens.checkGlyphSlot)
                .alpha(if (enabled) 1f else DISABLED_ALPHA),
            contentAlignment = Alignment.Center,
        ) {
            CheckGlyph(state)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FloordayDimens.spaceXs + FloordayDimens.spaceXxs),
                modifier = Modifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (state == CheckState.Skipped) scheme.onSurfaceVariant else scheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (partOfMinimum) {
                    Icon(
                        imageVector = FloordayIcons.Suelo,
                        contentDescription = stringResource(Res.string.check_part_of_minimum),
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(FloordayDimens.sueloGlyph),
                    )
                }
            }
            SupportingLine(state = state, supportingText = supportingText, doneTime = doneTime)
        }
    }
}

/** "08:00 · Done 08:42", with "Done" in the primary ink. Stays at full contrast when disabled. */
@Composable
private fun SupportingLine(state: CheckState, supportingText: String?, doneTime: String?) {
    val showDone = state == CheckState.Done && doneTime != null
    if (supportingText == null && !showDone) return
    val primary = MaterialTheme.colorScheme.primary
    val doneLabel = stringResource(Res.string.check_done_label)
    val text = buildAnnotatedString {
        if (supportingText != null) append(supportingText)
        if (showDone) {
            if (supportingText != null) append(" · ")
            withStyle(SpanStyle(color = primary, fontWeight = FontWeight.SemiBold)) { append(doneLabel) }
            append(" ")
            append(doneTime)
        }
    }
    Text(text = text, style = FloordayTheme.type.time, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun CheckGlyph(state: CheckState) {
    val colors = FloordayTheme.colors
    val reduced = LocalReducedMotion.current
    val done = state == CheckState.Done
    val corner by animateFloatAsState(
        targetValue = if (done) CORNER_DONE else CORNER_PENDING,
        animationSpec = FloordayMotion.state,
        label = "glyphCorner",
    )
    val fill by animateColorAsState(
        targetValue = when (state) {
            CheckState.Pending -> Color.Transparent
            CheckState.Done -> colors.checkDoneFill
            CheckState.Skipped -> colors.skippedFill
        },
        animationSpec = FloordayMotion.colorFx,
        label = "glyphFill",
    )
    val draw by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = if (done) FloordayMotion.state else FloordayMotion.undoDraw,
        label = "checkDraw",
    )
    val dash by animateFloatAsState(
        targetValue = if (state == CheckState.Skipped) 1f else 0f,
        animationSpec = FloordayMotion.state,
        label = "dashDraw",
    )
    val pop = remember { Animatable(1f) }
    LaunchedEffect(done) {
        if (done && !reduced && pop.value == 1f) {
            pop.snapTo(FloordayMotion.GLYPH_POP_FROM)
            pop.animateTo(1f, FloordayMotion.glyphPop)
        }
    }
    val shape = RoundedCornerShape(percent = corner.roundToInt())
    val check = remember { Path() }
    val segment = remember { Path() }
    val measure = remember { PathMeasure() }

    Box(
        modifier = Modifier
            .size(FloordayDimens.checkGlyph)
            .graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
            }
            .background(fill, shape)
            .then(
                if (!done) Modifier.border(FloordayDimens.checkGlyphBorder, colors.checkBorder, shape) else Modifier,
            )
            .drawBehind {
                val unit = size.width / GLYPH_VIEWPORT
                val stroke = FloordayDimens.checkMarkStroke.toPx()
                if (draw > 0f) {
                    check.reset()
                    check.moveTo(CHECK_X0 * unit, CHECK_Y0 * unit)
                    check.lineTo(CHECK_X1 * unit, CHECK_Y1 * unit)
                    check.lineTo(CHECK_X2 * unit, CHECK_Y2 * unit)
                    measure.setPath(check, false)
                    segment.reset()
                    measure.getSegment(0f, measure.length * draw, segment, true)
                    drawPath(
                        path = segment,
                        color = colors.checkDoneMark,
                        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
                if (dash > 0f) {
                    drawLine(
                        color = colors.skippedMark,
                        start = Offset(DASH_X0 * unit, DASH_Y * unit),
                        end = Offset((DASH_X0 + DASH_LENGTH * dash) * unit, DASH_Y * unit),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }
            },
    )
}

// Check and dash in a 24-unit box.
private const val GLYPH_VIEWPORT = 24f
private const val CHECK_X0 = 7f
private const val CHECK_Y0 = 12.5f
private const val CHECK_X1 = 10.5f
private const val CHECK_Y1 = 16f
private const val CHECK_X2 = 17f
private const val CHECK_Y2 = 8.5f
private const val DASH_X0 = 7f
private const val DASH_Y = 12f
private const val DASH_LENGTH = 10f
