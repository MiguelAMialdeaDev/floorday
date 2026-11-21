package studio.mialdea.floorday.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import studio.mialdea.floorday.core.data.db.FloordayConverters
import studio.mialdea.floorday.core.domain.model.DeviceState
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.repository.DeviceStateRepository

/** "device_state" DataStore, stored where the backup never reaches. */
class DataStoreDeviceStateRepository(
    private val dataStore: DataStore<Preferences>,
    private val ids: IdGenerator = IdGenerator.Random,
) : DeviceStateRepository {

    override val state: Flow<DeviceState> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toDeviceState() }
        .distinctUntilChanged()

    override suspend fun ensureInstallId(): Boolean {
        var created = false
        dataStore.edit { prefs ->
            if (prefs[Keys.INSTALL_ID]?.let(Uuid::parseOrNull) == null) {
                prefs[Keys.INSTALL_ID] = ids.newUuid().toString()
                created = true
            }
        }
        return created
    }

    override suspend fun update(transform: (DeviceState) -> DeviceState) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toDeviceState())) }
    }

    private object Keys {
        val INSTALL_ID = stringPreferencesKey("install_id")
        val EXACT_ALARM_PROMPT_SHOWN = booleanPreferencesKey("exact_alarm_prompt_shown")
        val NOTIFICATION_DENIALS = intPreferencesKey("notification_denials")
        val LAST_WATCHDOG_RUN_MS = longPreferencesKey("last_watchdog_run_ms")

        /** ISO yyyy-MM-dd, same as the dates in Room. */
        val PENDING_FREEZE_NOTICE = stringPreferencesKey("pending_freeze_notice")
    }

    private fun Preferences.toDeviceState(): DeviceState = DeviceState(
        installId = this[Keys.INSTALL_ID]?.let(Uuid::parseOrNull),
        exactAlarmPromptShown = this[Keys.EXACT_ALARM_PROMPT_SHOWN] ?: false,
        notificationDenials = (this[Keys.NOTIFICATION_DENIALS] ?: 0).coerceAtLeast(0),
        lastWatchdogRun = this[Keys.LAST_WATCHDOG_RUN_MS]?.let(Instant::fromEpochMilliseconds),
        pendingFreezeNotice = this[Keys.PENDING_FREEZE_NOTICE]?.let {
            runCatching { FloordayConverters.textToLocalDate(it) }.getOrNull()
        },
    )

    /** installId is never written here: only ensureInstallId manages it. */
    private fun MutablePreferences.write(state: DeviceState) {
        this[Keys.EXACT_ALARM_PROMPT_SHOWN] = state.exactAlarmPromptShown
        this[Keys.NOTIFICATION_DENIALS] = state.notificationDenials.coerceAtLeast(0)
        setOrRemove(Keys.LAST_WATCHDOG_RUN_MS, state.lastWatchdogRun?.toEpochMilliseconds())
        setOrRemove(Keys.PENDING_FREEZE_NOTICE, FloordayConverters.localDateToText(state.pendingFreezeNotice))
    }
}
