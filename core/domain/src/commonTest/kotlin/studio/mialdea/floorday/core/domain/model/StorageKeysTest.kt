package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Keys are stored in Room and in the exported JSON. Changing them requires a migration. */
class StorageKeysTest {

    @Test
    fun `claves de HabitKind`() {
        assertEquals(listOf("check", "medication"), HabitKind.entries.map { it.key })
        HabitKind.entries.forEach { assertEquals(it, HabitKind.fromKey(it.key)) }
        assertNull(HabitKind.fromKey("count"))
    }

    @Test
    fun `claves de ReminderMode`() {
        assertEquals(listOf("none", "gentle", "insistent"), ReminderMode.entries.map { it.key })
        ReminderMode.entries.forEach { assertEquals(it, ReminderMode.fromKey(it.key)) }
        assertNull(ReminderMode.fromKey("loud"))
    }

    @Test
    fun `claves de CompletionStatus`() {
        assertEquals(listOf("done", "skipped"), CompletionStatus.entries.map { it.key })
        CompletionStatus.entries.forEach { assertEquals(it, CompletionStatus.fromKey(it.key)) }
        assertNull(CompletionStatus.fromKey("DONE"))
    }

    @Test
    fun `claves de EntrySource`() {
        assertEquals(listOf("app", "notification", "widget"), EntrySource.entries.map { it.key })
        EntrySource.entries.forEach { assertEquals(it, EntrySource.fromKey(it.key)) }
        assertNull(EntrySource.fromKey("import"))
    }

    @Test
    fun `capacidades de cada tipo y hora obligatoria por recordatorio`() {
        assertEquals(listOf(false, false, false, false), HabitKind.CHECK.capabilities())
        assertEquals(listOf(true, true, true, true), HabitKind.MEDICATION.capabilities())
        assertEquals(listOf(false, false, true), ReminderMode.entries.map { it.requiresTime })
    }

    private fun HabitKind.capabilities(): List<Boolean> =
        listOf(allowsSkip, allowsAmount, allowsUnitLabel, allowsInsistentReminder)
}
