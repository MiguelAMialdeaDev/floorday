package studio.mialdea.floorday.core.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.data.db.entity.CompletionEntity

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completion WHERE day = :day ORDER BY recorded_at, slot_id")
    fun observeDay(day: LocalDate): Flow<List<CompletionEntity>>

    @Upsert
    suspend fun upsert(completion: CompletionEntity)

    @Query("DELETE FROM completion WHERE slot_id = :slotId AND day = :day")
    suspend fun delete(slotId: Uuid, day: LocalDate)
}
