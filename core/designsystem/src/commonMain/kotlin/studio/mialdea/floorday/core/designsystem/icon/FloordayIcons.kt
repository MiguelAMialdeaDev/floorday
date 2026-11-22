package studio.mialdea.floorday.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-made icons, no material-icons (material3 no longer brings it since CMP 1.8 and the
 * artifact is frozen). The colour comes from the tint of Icon. The check and the dash of
 * CheckRow are drawn paths instead, so they can draw on.
 */
@Suppress("MagicNumber")
object FloordayIcons {

    /** "Part of the minimum": a short floor line with a tick above it. Marks it by shape, not colour. */
    val Suelo: ImageVector by lazy {
        strokeIcon("Suelo", viewport = 16f, stroke = 1.6f) {
            moveTo(3f, 13f)
            lineTo(13f, 13f)
            moveTo(8f, 4.5f)
            lineTo(8f, 9.5f)
        }
    }

    private fun strokeIcon(
        name: String,
        viewport: Float,
        stroke: Float,
        block: PathBuilder.() -> Unit,
    ): ImageVector =
        ImageVector.Builder(
            name = "Floorday.$name",
            defaultWidth = viewport.dp,
            defaultHeight = viewport.dp,
            viewportWidth = viewport,
            viewportHeight = viewport,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = stroke,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = block,
            )
        }.build()
}
