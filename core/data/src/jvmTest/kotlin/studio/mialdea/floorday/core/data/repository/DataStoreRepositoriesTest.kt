package studio.mialdea.floorday.core.data.repository

import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.PreferencesFileSerializer
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.data.datastore.createPreferencesDataStore
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.model.UserSettings
import studio.mialdea.floorday.core.domain.model.WeightUnit
import studio.mialdea.floorday.core.domain.time.DayStart
import studio.mialdea.floorday.core.testing.builder.uuidOf
import studio.mialdea.floorday.core.testing.time.TestDates

/**
 * DataStore allows one active instance per file: each test cancels its Job before opening
 * the file again.
 */
class DataStoreRepositoriesTest {
    private val dir = createTempDirectory("floorday-datastore").toFile()
    private val settingsFile = File(dir, "user_settings.preferences_pb")
    private val deviceFile = File(dir, "device_state.preferences_pb")

    private fun settings(job: Job) = DataStoreUserSettingsRepository(
        createPreferencesDataStore(FileStorage(PreferencesFileSerializer) { settingsFile }, Dispatchers.IO, job),
    )

    private fun device(job: Job, ids: IdGenerator = IdGenerator.Random) = DataStoreDeviceStateRepository(
        createPreferencesDataStore(FileStorage(PreferencesFileSerializer) { deviceFile }, Dispatchers.IO, job),
        ids,
    )

    @Test
    fun `sin fichero los ajustes son los de por defecto`() = runTest {
        val job = SupervisorJob()

        assertEquals(UserSettings(), settings(job).settings.first())

        job.cancelAndJoin()
    }

    @Test
    fun `los ajustes se guardan y sobreviven a otra instancia`() = runTest {
        val first = SupervisorJob()
        settings(first).update {
            it.copy(onboardingDone = true, gentleReminderTime = LocalTime(21, 30), weightUnit = WeightUnit.KG)
        }
        first.cancelAndJoin()

        val second = SupervisorJob()
        val read = settings(second).settings.first()

        assertTrue(read.onboardingDone)
        assertEquals(LocalTime(21, 30), read.gentleReminderTime)
        assertEquals(WeightUnit.KG, read.weightUnit)
        second.cancelAndJoin()
    }

    @Test
    fun `un inicio de dia fuera de rango se guarda como las 4`() = runTest {
        val job = SupervisorJob()
        val repository = settings(job)

        repository.update { it.copy(dayStartMinutes = 9 * 60) }

        assertEquals(DayStart.DEFAULT_MINUTES, repository.settings.first().dayStartMinutes)
        job.cancelAndJoin()
    }

    @Test
    fun `quitar el recordatorio suave borra la clave`() = runTest {
        val job = SupervisorJob()
        val repository = settings(job)
        repository.update { it.copy(gentleReminderTime = LocalTime(9, 0)) }

        repository.update { it.copy(gentleReminderTime = null) }

        assertNull(repository.settings.first().gentleReminderTime)
        job.cancelAndJoin()
    }

    @Test
    fun `un fichero corrupto vuelve a los ajustes por defecto`() = runTest {
        settingsFile.writeBytes(ByteArray(64) { 0xFF.toByte() })
        val job = SupervisorJob()

        assertEquals(UserSettings(), settings(job).settings.first())

        job.cancelAndJoin()
    }

    @Test
    fun `el installId se crea una sola vez`() = runTest {
        val job = SupervisorJob()
        val repository = device(job, ids = { uuidOf(7) })

        assertTrue(repository.ensureInstallId())
        assertFalse(repository.ensureInstallId())

        assertEquals(uuidOf(7), repository.state.first().installId)
        job.cancelAndJoin()
    }

    @Test
    fun `update no puede cambiar el installId`() = runTest {
        val job = SupervisorJob()
        val repository = device(job, ids = { uuidOf(7) })
        repository.ensureInstallId()

        repository.update {
            it.copy(installId = uuidOf(8), notificationDenials = 2, pendingFreezeNotice = TestDates.SPRINT0_END)
        }

        val state = repository.state.first()
        assertEquals(uuidOf(7), state.installId)
        assertEquals(2, state.notificationDenials)
        assertEquals(TestDates.SPRINT0_END, state.pendingFreezeNotice)
        job.cancelAndJoin()
    }

    @Test
    fun `los ajustes y el estado del dispositivo van en ficheros separados`() = runTest {
        val job = SupervisorJob()
        settings(job).update { it.copy(onboardingDone = true) }
        device(job).ensureInstallId()

        job.cancelAndJoin()

        assertTrue(settingsFile.exists())
        assertTrue(deviceFile.exists())
    }
}
