package studio.mialdea.floorday.core.data.db.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.EntrySource

/** One row per (slot, logical day). Undo deletes the row. */
@Entity(
    tableName = "completion",
    primaryKeys = ["slot_id", "day"],
    foreignKeys = [
        ForeignKey(
            entity = HabitSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["slot_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["slot_id"]), Index(value = ["day"])],
)
data class CompletionEntity(
    @ColumnInfo(name = "slot_id") val slotId: Uuid,
    @ColumnInfo(name = "day") val day: LocalDate,
    @ColumnInfo(name = "status") val status: CompletionStatus,
    @ColumnInfo(name = "recorded_at") val recordedAt: Instant,
    @ColumnInfo(name = "amount_quarters") val amountQuarters: Int,
    @ColumnInfo(name = "source") val source: EntrySource,
)
