package studio.mialdea.floorday.core.testing.builder

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.HabitSlot
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.domain.model.SlotId
import studio.mialdea.floorday.core.testing.time.FakeClock
import studio.mialdea.floorday.core.testing.time.TestDates

@DslMarker
annotation class FloordayTestDsl

/** Uuid determinista y legible en los fallos: uuidOf(1) = 00000000-0000-0000-0000-000000000001. */
fun uuidOf(n: Int): Uuid = Uuid.fromLongs(0L, n.toLong())

fun habitIdOf(n: Int): HabitId = HabitId(uuidOf(n))

fun slotIdOf(n: Int): SlotId = SlotId(uuidOf(n))

@FloordayTestDsl
class HabitSlotBuilder internal constructor() {
    var id: SlotId = SlotId(Uuid.random())
    var label: String = "Mañana"
    var time: LocalTime? = null
    var amount: Quarters = Quarters.ONE
    var reminder: ReminderMode = ReminderMode.NONE

    internal fun build(habitId: HabitId): HabitSlot = HabitSlot(
        id = id,
        habitId = habitId,
        label = label,
        time = time,
        amount = amount,
        reminder = reminder,
    )
}

@FloordayTestDsl
class HabitBuilder internal constructor() {
    var id: HabitId = HabitId(Uuid.random())
    var name: String = "Beber agua"
    var kind: HabitKind = HabitKind.CHECK
    var days: DaysOfWeek = DaysOfWeek.EVERY_DAY

    /** null = fuera del Día Mínimo. */
    var minimumSince: LocalDate? = TestDates.BASELINE
    var archivedOn: LocalDate? = null
    var sortOrder: Int = 0
    var unitLabel: String? = null
    var createdAt: Instant = FakeClock.DEFAULT_START
    var updatedAt: Instant? = null

    internal var defaultSlot: HabitSlotBuilder.() -> Unit = {}
    private val slots = mutableListOf<HabitSlotBuilder>()

    fun slot(block: HabitSlotBuilder.() -> Unit = {}) {
        slots += HabitSlotBuilder().apply(block)
    }

    internal fun build(): Habit {
        val builders = slots.ifEmpty { listOf(HabitSlotBuilder().apply(defaultSlot)) }
        return Habit(
            id = id,
            name = name,
            kind = kind,
            slots = builders.map { it.build(id) },
            days = days,
            minimumSince = minimumSince,
            archivedOn = archivedOn,
            sortOrder = sortOrder,
            unitLabel = unitLabel,
            createdAt = createdAt,
            updatedAt = updatedAt ?: createdAt,
        )
    }
}

/** Hábito CHECK válido con un momento si no declaras ninguno. */
fun aHabit(block: HabitBuilder.() -> Unit = {}): Habit = HabitBuilder().apply(block).build()

/** Medicación ficticia: por defecto un momento a las 08:00, 1 unidad, recordatorio insistente. */
fun aMedication(block: HabitBuilder.() -> Unit = {}): Habit = HabitBuilder().apply {
    kind = HabitKind.MEDICATION
    name = "Vitamina D"
    unitLabel = "pastilla"
    defaultSlot = {
        time = LocalTime(8, 0)
        reminder = ReminderMode.INSISTENT
    }
    block()
}.build()
