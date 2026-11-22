package studio.mialdea.floorday.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Contained radii: the app rests on the floor, it does not float. Pills are only for chips (S4). */
internal val FloordayShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** Radius of a shape drawn [inset] inside another one, so both curves stay parallel. */
fun concentric(outer: Dp, inset: Dp): Dp = (outer - inset).coerceAtLeast(4.dp)
