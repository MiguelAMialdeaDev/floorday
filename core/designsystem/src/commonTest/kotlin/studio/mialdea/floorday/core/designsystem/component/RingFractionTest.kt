package studio.mialdea.floorday.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertEquals

class RingFractionTest {

    @Test
    fun `sin nada hecho el anillo esta vacio`() = assertEquals(0f, ringFraction(done = 0, required = 4))

    @Test
    fun `la mitad hecha llena medio anillo`() = assertEquals(0.5f, ringFraction(done = 2, required = 4))

    @Test
    fun `todo hecho llena el anillo`() = assertEquals(1f, ringFraction(done = 4, required = 4))

    @Test
    fun `hacer mas de lo requerido no pasa de lleno`() = assertEquals(1f, ringFraction(done = 5, required = 4))

    @Test
    fun `un valor negativo deja el anillo vacio`() = assertEquals(0f, ringFraction(done = -1, required = 4))

    @Test
    fun `un dia sin requeridos deja el anillo vacio`() = assertEquals(0f, ringFraction(done = 3, required = 0))
}
