package studio.mialdea.floorday.core.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.data.db.entity.HabitEntity
import studio.mialdea.floorday.core.data.db.entity.HabitSlotEntity
import studio.mialdea.floorday.core.data.db.entity.HabitWithSlots

@Dao
interface HabitDao {
    @Transaction
    @Query(
        "SELECT * FROM habit WHERE (:includeArchived OR archived_on IS NULL) " +
            "ORDER BY sort_order, name",
    )
    fun observeAll(includeArchived: Boolean): Flow<List<HabitWithSlots>>

    @Transaction
    @Query("SELECT * FROM habit WHERE id = :id")
    suspend fun getById(id: Uuid): HabitWithSlots?

    /** Upsert, never REPLACE: REPLACE deletes the row and the CASCADE would take slots and history. */
    @Upsert
    suspend fun upsertHabit(habit: HabitEntity)

    @Upsert
    suspend fun upsertSlots(slots: List<HabitSlotEntity>)

    /** `keep` is never empty: a Habit always has at least one slot. */
    @Query("DELETE FROM habit_slot WHERE habit_id = :habitId AND id NOT IN (:keep)")
    suspend fun deleteSlotsNotIn(habitId: Uuid, keep: List<Uuid>)

    @Query("UPDATE habit SET archived_on = :day WHERE id = :id")
    suspend fun setArchivedOn(id: Uuid, day: LocalDate?)

    @Query("DELETE FROM habit WHERE id = :id")
    suspend fun delete(id: Uuid)

    @Query("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM habit")
    suspend fun nextSortOrder(): Int
}
