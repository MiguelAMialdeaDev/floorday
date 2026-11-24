package studio.mialdea.floorday.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import studio.mialdea.floorday.core.designsystem.theme.FloordayDimens
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme

/**
 * One day of the week grid, told by shape and not only by colour (ADR-0015):
 * done is a filled Pino square with a check, missed a grey cross (never red), open an empty
 * outline and rest a small dot. It is not touchable; the caller gives it its semantics.
 */
@Composable
fun DayMark(mark: DayMarkState, modifier: Modifier = Modifier) {
    val colors = FloordayTheme.colors
    val shape = RoundedCornerShape(percent = CORNER_PERCENT)
    val base = modifier.aspectRatio(1f)
    val styled = when (mark) {
        DayMarkState.DONE -> base.background(colors.checkDoneFill, shape)
        DayMarkState.MISSED, DayMarkState.OPEN -> base.border(FloordayDimens.checkGlyphBorder, colors.checkBorder, shape)
        DayMarkState.REST -> base
    }
    Box(
        modifier = styled.drawBehind {
            val unit = size.width / VIEWPORT
            val stroke = Stroke(
                width = FloordayDimens.checkMarkStroke.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            )
            when (mark) {
                DayMarkState.DONE -> drawPath(
                    path = Path().apply {
                        moveTo(CHECK_X0 * unit, CHECK_Y0 * unit)
                        lineTo(CHECK_X1 * unit, CHECK_Y1 * unit)
                        lineTo(CHECK_X2 * unit, CHECK_Y2 * unit)
                    },
                    color = colors.checkDoneMark,
                    style = stroke,
                )
                DayMarkState.MISSED -> drawPath(
                    path = Path().apply {
                        moveTo(CROSS_LOW * unit, CROSS_LOW * unit)
                        lineTo(CROSS_HIGH * unit, CROSS_HIGH * unit)
                        moveTo(CROSS_HIGH * unit, CROSS_LOW * unit)
                        lineTo(CROSS_LOW * unit, CROSS_HIGH * unit)
                    },
                    color = colors.missedDay,
                    style = stroke,
                )
                DayMarkState.REST -> drawCircle(
                    color = colors.checkBorder,
                    radius = REST_DOT_RADIUS * unit,
                    center = Offset(size.width / 2, size.height / 2),
                )
                DayMarkState.OPEN -> Unit
            }
        },
    )
}

// Shapes in a 24-unit box, the same check as CheckRow.
private const val VIEWPORT = 24f
private const val CORNER_PERCENT = 22
private const val CHECK_X0 = 7f
private const val CHECK_Y0 = 12.5f
private const val CHECK_X1 = 10.5f
private const val CHECK_Y1 = 16f
private const val CHECK_X2 = 17f
private const val CHECK_Y2 = 8.5f
private const val CROSS_LOW = 8f
private const val CROSS_HIGH = 16f
private const val REST_DOT_RADIUS = 2f

/** What a [DayMark] shows. The feature maps its domain state to this one. */
enum class DayMarkState { DONE, MISSED, OPEN, REST }
