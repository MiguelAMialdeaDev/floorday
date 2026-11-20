package studio.mialdea.floorday.core.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** Datos ficticios del módulo. Los builders compartidos viven en :core:testing. */
internal object Fixtures {
    val habitId = HabitId.parse("00000000-0000-4000-8000-000000000001")
    val morningSlotId = SlotId.parse("00000000-0000-4000-8000-000000000011")
    val nightSlotId = SlotId.parse("00000000-0000-4000-8000-000000000012")

    val t0: Instant = Instant.parse("2026-11-23T08:00:00Z")

    /** 23-nov-2026 es lunes. */
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
    ) = HabitSlot(id, habitId, label, time, amount, reminder)

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

    /** Medicación ficticia de 1 por la mañana y ½ por la noche, ambas insistentes. */
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
