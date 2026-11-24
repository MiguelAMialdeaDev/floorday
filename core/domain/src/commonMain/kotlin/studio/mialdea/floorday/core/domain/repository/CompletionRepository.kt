package studio.mialdea.floorday.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.SlotId

interface CompletionRepository {
    /** `day` is the logical day, not the calendar date. */
    fun observeDay(day: LocalDate): Flow<List<Completion>>

    /** Marks of the logical days from [from] to [to], both included. */
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Completion>>

    /** A single row per (slotId, day): marking again replaces it. */
    suspend fun upsert(completion: Completion)

    /** Undo: deletes the row. If it does not exist, does nothing. */
    suspend fun remove(slotId: SlotId, day: LocalDate)
}
