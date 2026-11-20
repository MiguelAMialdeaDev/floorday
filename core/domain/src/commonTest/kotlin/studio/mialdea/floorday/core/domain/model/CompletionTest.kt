package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
}
