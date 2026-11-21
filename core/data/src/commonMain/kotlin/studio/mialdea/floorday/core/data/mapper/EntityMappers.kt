package studio.mialdea.floorday.core.data.mapper

import studio.mialdea.floorday.core.data.db.entity.CompletionEntity
import studio.mialdea.floorday.core.data.db.entity.HabitEntity
import studio.mialdea.floorday.core.data.db.entity.HabitSlotEntity
import studio.mialdea.floorday.core.data.db.entity.HabitWithSlots
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.HabitSlot
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.SlotId

internal fun HabitWithSlots.toDomain(): Habit = Habit(
    id = HabitId(habit.id),
    name = habit.name,
    kind = habit.kind,
    slots = slots.sortedBy { it.position }.map { it.toDomain() },
    days = DaysOfWeek(habit.days),
    minimumSince = habit.minimumSince,
    archivedOn = habit.archivedOn,
    sortOrder = habit.sortOrder,
    unitLabel = habit.unitLabel,
    createdAt = habit.createdAt,
    updatedAt = habit.updatedAt,
)

internal fun HabitSlotEntity.toDomain(): HabitSlot = HabitSlot(
    id = SlotId(id),
    habitId = HabitId(habitId),
    label = label,
    time = time,
    amount = Quarters(amountQuarters),
    reminder = reminder,
)

internal fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id.value,
    name = name,
    kind = kind,
    days = days.mask,
    minimumSince = minimumSince,
    archivedOn = archivedOn,
    sortOrder = sortOrder,
    unitLabel = unitLabel,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/** The position is the index in Habit.slots, so the user's order survives the round trip. */
internal fun Habit.slotEntities(): List<HabitSlotEntity> = slots.mapIndexed { position, slot ->
    HabitSlotEntity(
        id = slot.id.value,
        habitId = slot.habitId.value,
        position = position,
        label = slot.label,
        time = slot.time,
        amountQuarters = slot.amount.value,
        reminder = slot.reminder,
    )
}

internal fun CompletionEntity.toDomain(): Completion = Completion(
    slotId = SlotId(slotId),
    day = day,
    status = status,
    recordedAt = recordedAt,
    amount = Quarters(amountQuarters),
    source = source,
)

internal fun Completion.toEntity(): CompletionEntity = CompletionEntity(
    slotId = slotId.value,
    day = day,
    status = status,
    recordedAt = recordedAt,
    amountQuarters = amount.value,
    source = source,
)
