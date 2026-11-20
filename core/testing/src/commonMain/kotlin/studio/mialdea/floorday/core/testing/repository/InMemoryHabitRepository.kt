package studio.mialdea.floorday.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.repository.HabitRepository

/** El único HabitRepository en memoria del repo. Comparte store con InMemoryCompletionRepository. */
class InMemoryHabitRepository(
    private val store: InMemoryFloordayStore = InMemoryFloordayStore(),
) : HabitRepository {

    override fun observeHabits(includeArchived: Boolean): Flow<List<Habit>> =
        store.tables
            .map { t ->
                t.habits.values
                    .filter { includeArchived || it.archivedOn == null }
                    .sortedWith(compareBy({ it.sortOrder }, { it.name }))
            }
            .distinctUntilChanged()

    override suspend fun getHabit(id: HabitId): Habit? = store.tables.value.habits[id]

    override suspend fun save(habit: Habit) {
        // Igual que Room: sin momentos se borraría todo el historial del hábito.
        require(habit.slots.isNotEmpty()) { "Un hábito necesita al menos un momento" }
        store.write { t ->
            val kept = habit.slots.map { it.id }.toSet()
            val removed = t.habits[habit.id]?.slots.orEmpty().map { it.id }.filterNot { it in kept }.toSet()
            t.copy(
                habits = t.habits + (habit.id to habit),
                completions = t.completions.filterKeys { it.slotId !in removed },
            )
        }
    }

    override suspend fun archive(id: HabitId, on: LocalDate?) {
        store.write { t ->
            val habit = t.habits[id] ?: return@write t
            t.copy(habits = t.habits + (id to habit.copy(archivedOn = on)))
        }
    }

    override suspend fun delete(id: HabitId) {
        store.write { t ->
            val slotIds = t.habits[id]?.slots.orEmpty().map { it.id }.toSet()
            t.copy(
                habits = t.habits - id,
                completions = t.completions.filterKeys { it.slotId !in slotIds },
            )
        }
    }

    override suspend fun nextSortOrder(): Int =
        (store.tables.value.habits.values.maxOfOrNull { it.sortOrder } ?: -1) + 1

    /** Siembra sin suspender, para el arrange de los tests. */
    fun seed(vararg habits: Habit) {
        store.seed { t -> t.copy(habits = t.habits + habits.associateBy { it.id }) }
    }
}
