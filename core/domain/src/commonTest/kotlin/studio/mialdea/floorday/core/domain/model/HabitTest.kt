package studio.mialdea.floorday.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Fixtures.habit
import studio.mialdea.floorday.core.domain.model.Fixtures.monday
import studio.mialdea.floorday.core.domain.model.Fixtures.morningSlotId
import studio.mialdea.floorday.core.domain.model.Fixtures.nightSlotId
import studio.mialdea.floorday.core.domain.model.Fixtures.slot
import studio.mialdea.floorday.core.domain.model.Fixtures.slotIdFor
import studio.mialdea.floorday.core.domain.model.Fixtures.t0
import studio.mialdea.floorday.core.domain.model.Fixtures.vitaminD

class HabitTest {

    // Valid construction

    @Test
    fun `medicacion con dos momentos de 1 y medio`() {
        val habit = vitaminD()

        assertTrue(habit.isMedication)
        assertEquals(listOf(Quarters.ONE, Quarters.HALF), habit.slots.map { it.amount })
        assertEquals("pastilla", habit.unitLabel)
    }

    @Test
    fun `countsForMinimum se deriva de minimumSince`() {
        assertTrue(habit(minimumSince = monday).countsForMinimum)
        assertFalse(habit(minimumSince = null).countsForMinimum)
    }

    // Invariants of name, slots, days and timestamps

    @Test
    fun `reglas del nombre`() {
        assertFailsWith<IllegalArgumentException> { habit(name = "") }
        assertFailsWith<IllegalArgumentException> { habit(name = "   ") }
        assertFailsWith<IllegalArgumentException> { habit(name = "Caminar ") }
        assertFailsWith<IllegalArgumentException> { habit(name = "x".repeat(Habit.NAME_MAX_LENGTH + 1)) }
        assertEquals(Habit.NAME_MAX_LENGTH, habit(name = "x".repeat(Habit.NAME_MAX_LENGTH)).name.length)
    }

    @Test
    fun `reglas de los momentos`() {
        assertFailsWith<IllegalArgumentException> { habit(slots = emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            habit(slots = listOf(slot(id = morningSlotId), slot(id = morningSlotId)))
        }
        val otherHabit = HabitId.parse("00000000-0000-4000-8000-000000000099")
        assertFailsWith<IllegalArgumentException> { habit(slots = listOf(slot(habitId = otherHabit))) }
        val maxSlots = List(Habit.MAX_SLOTS) { i -> slot(id = slotIdFor(i)) }
        assertEquals(Habit.MAX_SLOTS, habit(slots = maxSlots).slots.size)
        val tooMany = List(Habit.MAX_SLOTS + 1) { i -> slot(id = slotIdFor(i)) }
        assertFailsWith<IllegalArgumentException> { habit(slots = tooMany) }
    }

    @Test
    fun `se conserva el orden de los momentos`() {
        val slots = listOf(slot(id = nightSlotId), slot(id = morningSlotId))

        assertEquals(listOf(nightSlotId, morningSlotId), habit(slots = slots).slots.map { it.id })
    }

    @Test
    fun `necesita al menos un dia de la semana`() {
        assertFailsWith<IllegalArgumentException> { habit(days = DaysOfWeek.NONE) }
    }

    @Test
    fun `marcas de tiempo y sortOrder`() {
        assertFailsWith<IllegalArgumentException> { habit(createdAt = t0, updatedAt = t0 - 1.minutes) }
        assertEquals(t0 + 1.minutes, habit(createdAt = t0, updatedAt = t0 + 1.minutes).updatedAt)
        assertFailsWith<IllegalArgumentException> { habit(sortOrder = -1) }
    }

    // Rules per kind

    @Test
    fun `un check no admite cantidad ni unidad ni recordatorio insistente`() {
        assertFailsWith<IllegalArgumentException> { habit(slots = listOf(slot(amount = Quarters.HALF))) }
        assertFailsWith<IllegalArgumentException> { habit(unitLabel = "vasos") }
        assertFailsWith<IllegalArgumentException> {
            habit(slots = listOf(slot(reminder = ReminderMode.INSISTENT)))
        }
    }

    @Test
    fun `un check admite el aviso suave sin hora`() {
        val h = habit(slots = listOf(slot(time = null, reminder = ReminderMode.GENTLE)))

        assertEquals(ReminderMode.GENTLE, h.slots.single().reminder)
    }

    @Test
    fun `reglas de la unidad en medicacion`() {
        assertNull(habit(kind = HabitKind.MEDICATION, unitLabel = null).unitLabel)
        assertFailsWith<IllegalArgumentException> { habit(kind = HabitKind.MEDICATION, unitLabel = " ") }
        val longest = "x".repeat(Habit.UNIT_LABEL_MAX_LENGTH)
        assertEquals(longest, habit(kind = HabitKind.MEDICATION, unitLabel = longest).unitLabel)
        assertFailsWith<IllegalArgumentException> {
            habit(kind = HabitKind.MEDICATION, unitLabel = "x".repeat(Habit.UNIT_LABEL_MAX_LENGTH + 1))
        }
    }

    // Scheduling per day

    @Test
    fun `solo se programa en los dias activos`() {
        val h = habit(days = DaysOfWeek.of(DayOfWeek.MONDAY))

        assertTrue(h.isScheduledOn(monday))
        assertFalse(h.isScheduledOn(LocalDate(2026, 11, 24)))
    }

    @Test
    fun `el dia de archivo ya no se programa pero los anteriores si`() {
        val archiveDay = LocalDate(2026, 11, 25)

        val h = habit(archivedOn = archiveDay)

        assertTrue(h.isArchived)
        assertTrue(h.isScheduledOn(LocalDate(2026, 11, 24)))
        assertFalse(h.isScheduledOn(archiveDay))
        assertFalse(h.isScheduledOn(LocalDate(2026, 11, 26)))
    }

    @Test
    fun `countsForMinimumOn respeta minimumSince y la programacion`() {
        val since = LocalDate(2026, 11, 25)

        val h = habit(minimumSince = since, days = DaysOfWeek.WEEKDAYS)

        assertFalse(h.countsForMinimumOn(LocalDate(2026, 11, 24)))
        assertTrue(h.countsForMinimumOn(since))
        assertFalse(h.countsForMinimumOn(LocalDate(2026, 11, 28))) // Saturday
        assertFalse(habit(minimumSince = null).countsForMinimumOn(monday))
    }

    // Recording

    @Test
    fun `record copia la cantidad del momento`() {
        val c = vitaminD().record(nightSlotId, monday, CompletionStatus.DONE, t0, EntrySource.NOTIFICATION)

        assertEquals(Quarters.HALF, c.amount)
        assertEquals(CompletionKey(nightSlotId, monday), c.key)
        assertEquals(EntrySource.NOTIFICATION, c.source)
        assertEquals(t0, c.recordedAt)
    }

    @Test
    fun `la cantidad registrada sobrevive a un cambio de cantidad`() {
        val original = vitaminD()
        val c = original.record(nightSlotId, monday, CompletionStatus.DONE, t0, EntrySource.APP)

        val edited = original.copy(
            slots = original.slots.map { if (it.id == nightSlotId) it.copy(amount = Quarters.ONE) else it },
        )

        assertEquals(Quarters.ONE, edited.slot(nightSlotId)?.amount)
        assertEquals(Quarters.HALF, c.amount)
    }

    @Test
    fun `omitir a proposito solo en medicacion`() {
        assertTrue(vitaminD().canRecord(CompletionStatus.SKIPPED))
        val skipped = vitaminD().record(morningSlotId, monday, CompletionStatus.SKIPPED, t0, EntrySource.APP)
        assertEquals(CompletionStatus.SKIPPED, skipped.status)
        assertFalse(habit().canRecord(CompletionStatus.SKIPPED))
        assertFailsWith<IllegalArgumentException> {
            habit().record(morningSlotId, monday, CompletionStatus.SKIPPED, t0, EntrySource.APP)
        }
    }

    @Test
    fun `record rechaza un momento de otro habito`() {
        val foreign = SlotId.parse("00000000-0000-4000-8000-0000000000ff")

        assertFailsWith<IllegalArgumentException> {
            vitaminD().record(foreign, monday, CompletionStatus.DONE, t0, EntrySource.APP)
        }
    }
}
