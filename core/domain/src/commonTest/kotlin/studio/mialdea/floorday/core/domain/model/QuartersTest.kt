package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuartersTest {

    @Test
    fun `las constantes valen lo que dicen`() {
        assertEquals(1, Quarters.QUARTER.value)
        assertEquals(2, Quarters.HALF.value)
        assertEquals(3, Quarters.THREE_QUARTERS.value)
        assertEquals(4, Quarters.ONE.value)
    }

    @Test
    fun `of compone unidades enteras y fraccion`() {
        assertEquals(Quarters(6), Quarters.of(1, QuarterFraction.HALF))
        assertEquals(Quarters.ONE, Quarters.of(1))
        assertEquals(Quarters.HALF, Quarters.of(0, QuarterFraction.HALF))
        assertEquals(Quarters(11), Quarters.of(2, QuarterFraction.THREE_QUARTERS))
    }

    @Test
    fun `cada opcion del selector se descompone en unidades y fraccion`() {
        val expected = listOf(
            0 to QuarterFraction.QUARTER,
            0 to QuarterFraction.HALF,
            0 to QuarterFraction.THREE_QUARTERS,
            1 to QuarterFraction.NONE,
            1 to QuarterFraction.QUARTER,
            1 to QuarterFraction.HALF,
            1 to QuarterFraction.THREE_QUARTERS,
            2 to QuarterFraction.NONE,
        )

        val actual = Quarters.EDITOR_CHOICES.map { it.wholeUnits to it.fraction }

        assertEquals(expected, actual)
    }

    @Test
    fun `el selector va de un cuarto a dos unidades`() {
        assertEquals(8, Quarters.EDITOR_CHOICES.size)
        assertEquals(Quarters.QUARTER, Quarters.EDITOR_CHOICES.first())
        assertEquals(Quarters.of(2), Quarters.EDITOR_CHOICES.last())
    }

    @Test
    fun `todo el rango hace ida y vuelta por unidades y fraccion`() {
        for (value in Quarters.MIN_VALUE..Quarters.MAX_VALUE) {
            val q = Quarters(value)
            assertEquals(q, Quarters.of(q.wholeUnits, q.fraction))
        }
    }

    @Test
    fun `isWhole solo con multiplos de cuatro`() {
        assertTrue(Quarters.ONE.isWhole)
        assertTrue(Quarters(8).isWhole)
        assertFalse(Quarters.HALF.isWhole)
        assertFalse(Quarters(6).isWhole)
    }

    @Test
    fun `se ordena por cantidad`() {
        assertTrue(Quarters.HALF < Quarters.ONE)
        assertEquals(Quarters.ONE, listOf(Quarters.ONE, Quarters.QUARTER).max())
    }

    @Test
    fun `rechaza valores fuera de rango`() {
        assertFailsWith<IllegalArgumentException> { Quarters(0) }
        assertFailsWith<IllegalArgumentException> { Quarters(-4) }
        assertFailsWith<IllegalArgumentException> { Quarters(Quarters.MAX_VALUE + 1) }
        assertFailsWith<IllegalArgumentException> { Quarters.of(0) }
        assertFailsWith<IllegalArgumentException> { Quarters.of(-1, QuarterFraction.HALF) }
    }

    @Test
    fun `fromQuarters rechaza un resto invalido`() {
        assertFailsWith<IllegalArgumentException> { QuarterFraction.fromQuarters(4) }
    }
}
