package studio.mialdea.floorday.core.data.db

import kotlin.test.Test
import kotlin.test.assertEquals
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.EntrySource
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.ReminderMode

/**
 * Room stores enums by their `key`. If this test fails, a stored key changed or disappeared:
 * add a migration that rewrites the rows before touching the list.
 */
class StoredEnumKeysTest {
    @Test
    fun `claves de HabitKind`() =
        assertEquals(listOf("check", "medication"), HabitKind.entries.map(FloordayConverters::habitKindToKey))

    @Test
    fun `claves de ReminderMode`() = assertEquals(
        listOf("none", "gentle", "insistent"),
        ReminderMode.entries.map(FloordayConverters::reminderModeToKey),
    )

    @Test
    fun `claves de CompletionStatus`() = assertEquals(
        listOf("done", "skipped"),
        CompletionStatus.entries.map(FloordayConverters::completionStatusToKey),
    )

    @Test
    fun `claves de EntrySource`() = assertEquals(
        listOf("app", "notification", "widget"),
        EntrySource.entries.map(FloordayConverters::entrySourceToKey),
    )
}
