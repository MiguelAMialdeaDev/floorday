package studio.mialdea.floorday.core.data.db.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.ReminderMode

@Entity(
    tableName = "habit_slot",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id"])],
)
data class HabitSlotEntity(
    @PrimaryKey val id: Uuid,
    @ColumnInfo(name = "habit_id") val habitId: Uuid,
    /** Index of the slot in Habit.slots: the order the user sees. */
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "time_minutes") val time: LocalTime?,
    /** Quarters: 4 = 1, 2 = ½. */
    @ColumnInfo(name = "amount_quarters") val amountQuarters: Int,
    @ColumnInfo(name = "reminder") val reminder: ReminderMode,
)
