package studio.mialdea.floorday.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.Storage
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

/**
 * The context must carry a Job and live as long as the app. Only one active instance per file
 * and process: create it as a singleton. A corrupt file is replaced by empty preferences, since
 * losing settings is better than a crash loop.
 */
fun createPreferencesDataStore(
    storage: Storage<Preferences>,
    ioDispatcher: CoroutineDispatcher,
    job: Job = SupervisorJob(),
): DataStore<Preferences> = DataStore.Builder(storage, job + ioDispatcher)
    .setCorruptionHandler(ReplaceFileCorruptionHandler { emptyPreferences() })
    .build()
