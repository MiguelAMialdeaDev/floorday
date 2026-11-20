package studio.mialdea.floorday.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionKey
import studio.mialdea.floorday.core.domain.model.SlotId
import studio.mialdea.floorday.core.domain.repository.CompletionRepository
import studio.mialdea.floorday.core.testing.repository.InMemoryFloordayStore.Tables

class InMemoryCompletionRepository(
    private val store: InMemoryFloordayStore = InMemoryFloordayStore(),
) : CompletionRepository {

    override fun observeDay(day: LocalDate): Flow<List<Completion>> =
        store.tables
            .map { t -> t.completions.values.filter { it.day == day }.sortedBy { it.recordedAt } }
            .distinctUntilChanged()

    override suspend fun upsert(completion: Completion) {
        store.write { t -> t.withCompletion(completion) }
    }

    override suspend fun remove(slotId: SlotId, day: LocalDate) {
        store.write { t -> t.copy(completions = t.completions - CompletionKey(slotId, day)) }
    }

    fun seed(vararg completions: Completion) {
        store.seed { t -> completions.fold(t) { acc, c -> acc.withCompletion(c) } }
    }

    private fun Tables.withCompletion(completion: Completion): Tables {
        if (store.strictForeignKeys) {
            check(slotExists(completion.slotId)) {
                "FK: no existe el momento ${completion.slotId}. Siembra antes el hábito."
            }
        }
        return copy(completions = completions + (completion.key to completion))
    }
}
