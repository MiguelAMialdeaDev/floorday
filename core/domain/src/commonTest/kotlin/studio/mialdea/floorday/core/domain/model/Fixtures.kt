package studio.mialdea.floorday.core.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** Fake data for this module. The shared builders live in :core:testing. */
internal object Fixtures {
    val habitId = HabitId.parse("00000000-0000-4000-8000-000000000001")
    val morningSlotId = SlotId.parse("00000000-0000-4000-8000-000000000011")
    val nightSlotId = SlotId.parse("00000000-0000-4000-8000-000000000012")

    val t0: Instant = Instant.parse("2026-11-23T08:00:00Z")

    /** 23-Nov-2026 is a Monday. */
    val monday = LocalDate(2026, 11, 23)

    fun slotIdFor(index: Int): SlotId =
        SlotId.parse("00000000-0000-4000-8000-" + (100 + index).toString().padStart(12, '0'))

    fun slot(
        id: SlotId = morningSlotId,
        habitId: HabitId = this.habitId,
        label: String = "Mañana",
        time: LocalTime? = LocalTime(8, 0),
        amount: Quarters = Quarters.ONE,
        reminder: ReminderMode = ReminderMode.NONE,
        addedOn: LocalDate? = null,
        removedOn: LocalDate? = null,
    ) = HabitSlot(id, habitId, label, time, amount, reminder, addedOn, removedOn)

    fun habit(
        kind: HabitKind = HabitKind.CHECK,
        name: String = "Caminar",
        slots: List<HabitSlot> = listOf(slot()),
        days: DaysOfWeek = DaysOfWeek.EVERY_DAY,
        minimumSince: LocalDate? = monday,
        archivedOn: LocalDate? = null,
        sortOrder: Int = 0,
        unitLabel: String? = null,
        createdAt: Instant = t0,
        updatedAt: Instant = createdAt,
    ) = Habit(
        id = habitId,
        name = name,
        kind = kind,
        slots = slots,
        days = days,
        minimumSince = minimumSince,
        archivedOn = archivedOn,
        sortOrder = sortOrder,
        unitLabel = unitLabel,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    /** Fake medication of 1 in the morning and ½ at night, both insistent. */
    fun vitaminD() = habit(
        kind = HabitKind.MEDICATION,
        name = "Vitamina D",
        unitLabel = "pastilla",
        slots = listOf(
            slot(
                id = morningSlotId,
                label = "Mañana",
                time = LocalTime(8, 0),
                amount = Quarters.ONE,
                reminder = ReminderMode.INSISTENT,
            ),
            slot(
                id = nightSlotId,
                label = "Noche",
                time = LocalTime(23, 30),
                amount = Quarters.HALF,
                reminder = ReminderMode.INSISTENT,
            ),
        ),
    )
}
