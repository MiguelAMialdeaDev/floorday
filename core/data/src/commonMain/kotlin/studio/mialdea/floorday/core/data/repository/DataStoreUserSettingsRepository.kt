package studio.mialdea.floorday.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import studio.mialdea.floorday.core.data.db.FloordayConverters
import studio.mialdea.floorday.core.domain.model.UserSettings
import studio.mialdea.floorday.core.domain.model.WeightUnit
import studio.mialdea.floorday.core.domain.repository.UserSettingsRepository

/** "user_settings" DataStore. Values are sanitized on read and on write. */
class DataStoreUserSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : UserSettingsRepository {

    override val settings: Flow<UserSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toUserSettings() }
        .distinctUntilChanged()

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toUserSettings()).sanitized()) }
    }

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val DAY_START_MINUTES = intPreferencesKey("day_start_minutes")
        val GENTLE_REMINDER_MINUTES = intPreferencesKey("gentle_reminder_minutes")
        val INSIST_INTERVAL_MIN = intPreferencesKey("insist_interval_min")
        val INSIST_MAX_RETRIES = intPreferencesKey("insist_max_retries")
        val SHOW_NAME_ON_LOCK_SCREEN = booleanPreferencesKey("show_name_on_lock_screen")

        /** WeightUnit.key ("kg", "lb", "st"); an unknown value falls back to the locale. */
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val HAPTICS = booleanPreferencesKey("haptics")
    }

    private fun Preferences.toUserSettings(): UserSettings {
        val defaults = UserSettings()
        return UserSettings(
            onboardingDone = this[Keys.ONBOARDING_DONE] ?: defaults.onboardingDone,
            dayStartMinutes = this[Keys.DAY_START_MINUTES] ?: defaults.dayStartMinutes,
            gentleReminderTime = FloordayConverters.minutesToLocalTime(
                this[Keys.GENTLE_REMINDER_MINUTES]?.takeIf { it in 0 until MINUTES_PER_DAY },
            ),
            insistIntervalMin = this[Keys.INSIST_INTERVAL_MIN] ?: defaults.insistIntervalMin,
            insistMaxRetries = this[Keys.INSIST_MAX_RETRIES] ?: defaults.insistMaxRetries,
            showNameOnLockScreen = this[Keys.SHOW_NAME_ON_LOCK_SCREEN] ?: defaults.showNameOnLockScreen,
            weightUnit = this[Keys.WEIGHT_UNIT]?.let(WeightUnit::fromKey),
            haptics = this[Keys.HAPTICS] ?: defaults.haptics,
        ).sanitized()
    }

    private fun MutablePreferences.write(settings: UserSettings) {
        this[Keys.ONBOARDING_DONE] = settings.onboardingDone
        this[Keys.DAY_START_MINUTES] = settings.dayStartMinutes
        setOrRemove(Keys.GENTLE_REMINDER_MINUTES, FloordayConverters.localTimeToMinutes(settings.gentleReminderTime))
        this[Keys.INSIST_INTERVAL_MIN] = settings.insistIntervalMin
        this[Keys.INSIST_MAX_RETRIES] = settings.insistMaxRetries
        this[Keys.SHOW_NAME_ON_LOCK_SCREEN] = settings.showNameOnLockScreen
        setOrRemove(Keys.WEIGHT_UNIT, settings.weightUnit?.key)
        this[Keys.HAPTICS] = settings.haptics
    }

    private companion object {
        const val MINUTES_PER_DAY = 1_440
    }
}

internal fun <T> MutablePreferences.setOrRemove(key: Preferences.Key<T>, value: T?) {
    if (value == null) {
        remove(key)
    } else {
        this[key] = value
    }
}
