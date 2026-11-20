package studio.mialdea.floorday.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId

interface HabitRepository {
    /** Ordenados por sortOrder y después por nombre. */
    fun observeHabits(includeArchived: Boolean = false): Flow<List<Habit>>

    suspend fun getHabit(id: HabitId): Habit?

    /**
     * Inserta o actualiza el hábito y deja sus momentos exactamente como `habit.slots`,
     * en una sola transacción. Exige al menos un momento. Quitar un momento borra su historial.
     */
    suspend fun save(habit: Habit)

    /** `on = null` desarchiva. */
    suspend fun archive(id: HabitId, on: LocalDate?)

    /** Borra el hábito, sus momentos y su historial (CASCADE). */
    suspend fun delete(id: HabitId)

    /** sortOrder para un hábito nuevo: el máximo actual más 1, o 0 si no hay ninguno. */
    suspend fun nextSortOrder(): Int
}
