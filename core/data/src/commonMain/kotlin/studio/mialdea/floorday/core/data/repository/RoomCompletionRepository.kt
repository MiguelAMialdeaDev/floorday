package studio.mialdea.floorday.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.data.db.FloordayDatabase
import studio.mialdea.floorday.core.data.mapper.toDomain
import studio.mialdea.floorday.core.data.mapper.toEntity
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.SlotId
import studio.mialdea.floorday.core.domain.repository.CompletionRepository

class RoomCompletionRepository(database: FloordayDatabase) : CompletionRepository {
    private val dao = database.completionDao()

    override fun observeDay(day: LocalDate): Flow<List<Completion>> =
        dao.observeDay(day)
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()

    override suspend fun upsert(completion: Completion) {
        dao.upsert(completion.toEntity())
    }

    override suspend fun remove(slotId: SlotId, day: LocalDate) {
        dao.delete(slotId.value, day)
    }
}
