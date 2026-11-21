package studio.mialdea.floorday.core.data.db.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.HabitKind

/** There is no counts_for_minimum column: it is derived from minimum_since. */
@Entity(
    tableName = "habit",
    indices = [Index(value = ["sort_order"])],
)
data class HabitEntity(
    @PrimaryKey val id: Uuid,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "kind") val kind: HabitKind,
    /** Mon..Sun bitmask, bit 0 = Monday (DaysOfWeek). */
    @ColumnInfo(name = "days") val days: Int,
    @ColumnInfo(name = "minimum_since") val minimumSince: LocalDate?,
    @ColumnInfo(name = "archived_on") val archivedOn: LocalDate?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "unit_label") val unitLabel: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
