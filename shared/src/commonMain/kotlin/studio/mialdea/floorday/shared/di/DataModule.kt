package studio.mialdea.floorday.shared.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import studio.mialdea.floorday.core.data.datastore.createPreferencesDataStore
import studio.mialdea.floorday.core.data.db.FloordayDatabase
import studio.mialdea.floorday.core.data.db.FloordayDatabaseFactory
import studio.mialdea.floorday.core.data.platform.DataPlatform
import studio.mialdea.floorday.core.data.repository.DataStoreDeviceStateRepository
import studio.mialdea.floorday.core.data.repository.DataStoreUserSettingsRepository
import studio.mialdea.floorday.core.data.repository.RoomCompletionRepository
import studio.mialdea.floorday.core.data.repository.RoomHabitRepository
import studio.mialdea.floorday.core.domain.repository.CompletionRepository
import studio.mialdea.floorday.core.domain.repository.DeviceStateRepository
import studio.mialdea.floorday.core.domain.repository.HabitRepository
import studio.mialdea.floorday.core.domain.repository.UserSettingsRepository

/**
 * Persistence for the whole app. Everything is a `single`: Room and each DataStore file must be
 * unique per process. Lives in :shared because Koin is not allowed in :core:*.
 */
fun dataModule(platform: DataPlatform): Module = module {
    single<FloordayDatabase> { FloordayDatabaseFactory.create(platform.databaseBuilder(), platform.ioDispatcher) }
    single<HabitRepository> { RoomHabitRepository(get()) }
    single<CompletionRepository> { RoomCompletionRepository(get()) }
    single<UserSettingsRepository> {
        DataStoreUserSettingsRepository(createPreferencesDataStore(platform.userSettingsStorage(), platform.ioDispatcher))
    }
    single<DeviceStateRepository> {
        DataStoreDeviceStateRepository(createPreferencesDataStore(platform.deviceStateStorage(), platform.ioDispatcher))
    }
}

/**
 * Data work for every process start. For now it creates the device_state file with the install id;
 * from S4 the returned "new install" flag drives the restore handling (ADR-0014).
 */
fun Koin.launchDataStartup(scope: CoroutineScope): Job = scope.launch {
    get<DeviceStateRepository>().ensureInstallId()
}
