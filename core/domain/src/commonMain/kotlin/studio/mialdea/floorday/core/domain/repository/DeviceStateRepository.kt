package studio.mialdea.floorday.core.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.mialdea.floorday.core.domain.model.DeviceState

interface DeviceStateRepository {
    val state: Flow<DeviceState>

    /** Creates the installId if missing. Returns true if it was just created (new install or restore). */
    suspend fun ensureInstallId(): Boolean

    /** Updates everything except installId, which only [ensureInstallId] manages. */
    suspend fun update(transform: (DeviceState) -> DeviceState)
}
