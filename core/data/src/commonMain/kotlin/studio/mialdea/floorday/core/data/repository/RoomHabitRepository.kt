package studio.mialdea.floorday.core.data.repository

import androidx.room3.withWriteTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.data.db.FloordayDatabase
import studio.mialdea.floorday.core.data.db.entity.HabitWithSlots
import studio.mialdea.floorday.core.data.mapper.slotEntities
import studio.mialdea.floorday.core.data.mapper.toDomain
import studio.mialdea.floorday.core.data.mapper.toEntity
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.repository.HabitRepository

class RoomHabitRepository(private val database: FloordayDatabase) : HabitRepository {
    private val dao = database.habitDao()

    override fun observeHabits(includeArchived: Boolean): Flow<List<Habit>> =
        dao.observeAll(includeArchived)
            .map { rows -> rows.map(HabitWithSlots::toDomain) }
            .distinctUntilChanged()

    override suspend fun getHabit(id: HabitId): Habit? = dao.getById(id.value)?.toDomain()

    override suspend fun save(habit: Habit) {
        // Habit already requires one slot; this guards the DELETE below, which with an empty list
        // would try to drop every slot. A dropped slot with records fails (RESTRICT) and rolls back.
        require(habit.slots.isNotEmpty()) { "A habit needs at least one slot" }
        database.withWriteTransaction {
            dao.upsertHabit(habit.toEntity())
            dao.deleteSlotsNotIn(habit.id.value, habit.slots.map { it.id.value })
            dao.upsertSlots(habit.slotEntities())
        }
    }

    override suspend fun archive(id: HabitId, on: LocalDate?) {
        dao.setArchivedOn(id.value, on)
    }

    override suspend fun delete(id: HabitId) {
        database.withWriteTransaction {
            dao.deleteCompletionsOf(id.value)
            dao.delete(id.value)
        }
    }

    override suspend fun nextSortOrder(): Int = dao.nextSortOrder()
}
