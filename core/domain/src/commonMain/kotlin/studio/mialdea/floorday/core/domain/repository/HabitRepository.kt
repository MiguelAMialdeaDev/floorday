package studio.mialdea.floorday.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId

interface HabitRepository {
    /** Sorted by sortOrder and then by name. */
    fun observeHabits(includeArchived: Boolean = false): Flow<List<Habit>>

    suspend fun getHabit(id: HabitId): Habit?

    /**
     * Inserts or updates the habit and leaves its slots exactly as `habit.slots`,
     * in a single transaction. Requires at least one slot. Removing a slot deletes its history.
     */
    suspend fun save(habit: Habit)

    /** `on = null` unarchives. */
    suspend fun archive(id: HabitId, on: LocalDate?)

    /** Deletes the habit, its slots and its history (CASCADE). */
    suspend fun delete(id: HabitId)

    /** sortOrder for a new habit: the current maximum plus 1, or 0 if there are none. */
    suspend fun nextSortOrder(): Int
}
