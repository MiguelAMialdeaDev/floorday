package studio.mialdea.floorday.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import studio.mialdea.floorday.core.domain.model.UserSettings
import studio.mialdea.floorday.core.domain.repository.UserSettingsRepository

class FakeUserSettingsRepository(initial: UserSettings = UserSettings()) : UserSettingsRepository {

    private val state = MutableStateFlow(initial)

    override val settings: Flow<UserSettings> = state.asStateFlow()

    var current: UserSettings
        get() = state.value
        set(value) {
            state.value = value
        }

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        state.update(transform)
    }
}
