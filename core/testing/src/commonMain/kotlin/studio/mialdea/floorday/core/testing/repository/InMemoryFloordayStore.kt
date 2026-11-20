package studio.mialdea.floorday.core.testing.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionKey
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitId
import studio.mialdea.floorday.core.domain.model.SlotId

/**
 * State shared by the in-memory repos. It reproduces the CASCADE FKs of schema v1
 * (habit -> habit_slot -> completion) and the atomicity of a transaction: each write
 * is a single update, so nobody observes an intermediate state.
 * Note: unlike Room, it does not re-emit if the content does not change.
 */
class InMemoryFloordayStore(val strictForeignKeys: Boolean = true) {

    internal data class Tables(
        val habits: Map<HabitId, Habit> = emptyMap(),
        val completions: Map<CompletionKey, Completion> = emptyMap(),
    ) {
        fun slotExists(slotId: SlotId): Boolean = habits.values.any { habit -> habit.slot(slotId) != null }
    }

    internal val tables = MutableStateFlow(Tables())

    /** If not null, every write throws this exception (disk full or corrupt DB). */
    var failWith: Throwable? = null

    /** If true, the next write fails just once (to test retries in the UI). */
    var failNextWrite: Boolean = false

    /** Normal write. The transformation must be pure: update may retry it. */
    internal fun write(transform: (Tables) -> Tables) {
        failWith?.let { throw it }
        if (failNextWrite) {
            failNextWrite = false
            error("Escritura fallida a propósito (test)")
        }
        tables.update(transform)
    }

    /** Seeding for a test's arrange step: ignores failWith and failNextWrite. */
    internal fun seed(transform: (Tables) -> Tables) {
        tables.update(transform)
    }

    fun clear() {
        tables.value = Tables()
    }
}
