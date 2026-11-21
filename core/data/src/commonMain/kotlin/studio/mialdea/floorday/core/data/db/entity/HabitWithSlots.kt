package studio.mialdea.floorday.core.data.db.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class HabitWithSlots(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["habit_id"])
    val slots: List<HabitSlotEntity>,
)
