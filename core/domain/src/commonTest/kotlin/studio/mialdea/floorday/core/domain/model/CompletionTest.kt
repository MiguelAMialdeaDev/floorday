package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import studio.mialdea.floorday.core.domain.model.Fixtures.monday
import studio.mialdea.floorday.core.domain.model.Fixtures.morningSlotId
import studio.mialdea.floorday.core.domain.model.Fixtures.t0
import studio.mialdea.floorday.core.domain.model.Fixtures.vitaminD

class CompletionTest {

    @Test
    fun `solo DONE cuenta como hecho`() {
        assertTrue(CompletionStatus.DONE.countsAsDone)
        assertFalse(CompletionStatus.SKIPPED.countsAsDone)
    }

    @Test
    fun `solo SKIPPED excusa el momento`() {
        assertTrue(CompletionStatus.SKIPPED.excusesSlot)
        assertFalse(CompletionStatus.DONE.excusesSlot)
    }

    @Test
    fun `la clave identifica momento y dia sin importar el estado`() {
        val done = vitaminD().record(morningSlotId, monday, CompletionStatus.DONE, t0, EntrySource.APP)

        val skipped = done.copy(status = CompletionStatus.SKIPPED)

        assertEquals(done.key, skipped.key)
    }
}
