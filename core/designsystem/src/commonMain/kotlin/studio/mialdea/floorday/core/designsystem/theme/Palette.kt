package studio.mialdea.floorday.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Floorday palette ("Suelo Quieto", ADR-0015). Names describe the material, not the use:
 * ColorScheme.kt decides the use. Scale: 50 is the lightest tone and 950 the darkest.
 * Every text and non-text pair the components draw is checked by ContrastTest.
 */
@Suppress("MagicNumber")
internal object Palette {
    // Pino: the one ink. Done and the minimum. Luminance falls steadily from 50 to 950.
    val Pino50 = Color(0xFFEBF4EF)
    val Pino100 = Color(0xFFD2E8DC)
    val Pino200 = Color(0xFFA3D4BC)
    val Pino300 = Color(0xFF74BA9A)
    val Pino400 = Color(0xFF4A9C7C)
    val Pino500 = Color(0xFF2C7D60)
    val Pino600 = Color(0xFF22654E)
    val Pino700 = Color(0xFF1F4A3D)
    val Pino800 = Color(0xFF173A30)
    val Pino900 = Color(0xFF0F2A22)
    val Pino950 = Color(0xFF0A1D17)

    // Barro: the streak (S4). Warmth without a flame.
    val Barro50 = Color(0xFFFBEEE5)
    val Barro100 = Color(0xFFF4DCCB)
    val Barro200 = Color(0xFFEEBB9C)
    val Barro600 = Color(0xFF8E4A2B)
    val Barro700 = Color(0xFF743B21)
    val Barro900 = Color(0xFF4A2411)
    val Barro950 = Color(0xFF32180A)

    // Pizarra: the freeze (S4). Cold, because it freezes the streak.
    val Pizarra50 = Color(0xFFEBF2F8)
    val Pizarra100 = Color(0xFFD6E4F0)
    val Pizarra200 = Color(0xFFC3D6EC)
    val Pizarra600 = Color(0xFF2F5470)
    val Pizarra700 = Color(0xFF28465F)
    val Pizarra900 = Color(0xFF132A3D)
    val Pizarra950 = Color(0xFF0C1B29)

    // Hueso and Tinta: warm light neutrals (paper and ink).
    val BlancoCalido = Color(0xFFFFFDF9)
    val HuesoBright = Color(0xFFFBF8F3)
    val Hueso = Color(0xFFF7F3EC)
    val Hueso100 = Color(0xFFF2EDE5)
    val Hueso200 = Color(0xFFECE6DC)
    val Hueso300 = Color(0xFFE6DFD4)
    val Hueso400 = Color(0xFFE0D8CC)
    val HuesoDim = Color(0xFFDDD6CB)
    val HuesoVariant = Color(0xFFE8E1D6)
    val Arena = Color(0xFFCFC6B8)
    val Piedra = Color(0xFF776F64)
    val TintaSuave = Color(0xFF4F4A42)
    val Tinta = Color(0xFF22201B)
    val TintaInversa = Color(0xFF32302B)
    val HuesoInverso = Color(0xFFF4EFE7)

    // Noche: warm dark neutrals, never the blue grey of default M3.
    val NocheLowest = Color(0xFF0F0E0B)
    val Noche = Color(0xFF14130F)
    val NocheLow = Color(0xFF1C1A16)
    val Noche200 = Color(0xFF211F1A)
    val Noche300 = Color(0xFF2B2924)
    val Noche400 = Color(0xFF36332D)
    val NocheVariant = Color(0xFF3A362F)
    val NocheBright = Color(0xFF3A3732)
    val NocheOutlineVariant = Color(0xFF4A453D)
    val NocheOutline = Color(0xFF968F83)
    val NocheTextoSuave = Color(0xFFC3BBAE)
    val NocheTexto = Color(0xFFECE6DA)

    // Baked tints, stored as hex so ContrastTest measures exactly what is drawn.
    /** Pino500 at 14 % over Hueso. */
    val RingTrackLight = Color(0xFFDBE2D8)

    /** Pino300 at 18 % over Noche. */
    val RingTrackDark = Color(0xFF253128)

    // Error: form validation only. Never for a missed day or dose.
    val Error = Color(0xFFA4262C)
    val ErrorContainer = Color(0xFFF9DAD7)
    val OnErrorContainer = Color(0xFF410E0B)
    val ErrorDark = Color(0xFFFFB4AB)
    val OnErrorDark = Color(0xFF690005)
    val ErrorContainerDark = Color(0xFF93000A)
    val OnErrorContainerDark = Color(0xFFFFDAD6)

    val Scrim = Color(0xFF000000)
}
