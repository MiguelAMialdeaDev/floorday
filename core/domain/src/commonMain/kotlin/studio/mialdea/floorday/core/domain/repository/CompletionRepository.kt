package studio.mialdea.floorday.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.SlotId

interface CompletionRepository {
    /** `day` es el día lógico, no la fecha civil. */
    fun observeDay(day: LocalDate): Flow<List<Completion>>

    /** Una sola fila por (slotId, day): marcar otra vez reemplaza. */
    suspend fun upsert(completion: Completion)

    /** Deshacer: borra la fila. Si no existe, no hace nada. */
    suspend fun remove(slotId: SlotId, day: LocalDate)
}
