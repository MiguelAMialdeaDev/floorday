package studio.mialdea.floorday.core.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.mialdea.floorday.core.domain.model.UserSettings

interface UserSettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun update(transform: (UserSettings) -> UserSettings)
}
