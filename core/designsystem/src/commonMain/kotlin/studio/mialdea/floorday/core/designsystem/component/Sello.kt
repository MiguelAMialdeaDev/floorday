package studio.mialdea.floorday.core.designsystem.component

import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val SELLO_LOBES = 8
private const val SELLO_DEPTH = 0.07f
private const val SELLO_STEPS = 96

/**
 * The scalloped seal (sello) that sets on the ring when the minimum is met: a circle whose
 * radius waves [lobes] times. Drawn by hand, so it needs no experimental shapes API.
 * The path fills a [size] x [size] box starting at (0, 0).
 */
internal fun selloPath(size: Float, lobes: Int = SELLO_LOBES, depth: Float = SELLO_DEPTH): Path = Path().apply {
    val c = size / 2f
    for (i in 0..SELLO_STEPS) {
        val t = 2.0 * PI * i / SELLO_STEPS
        val r = c * (1f - depth + depth * cos(lobes * t).toFloat())
        val x = c + r * cos(t).toFloat()
        val y = c + r * sin(t).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
