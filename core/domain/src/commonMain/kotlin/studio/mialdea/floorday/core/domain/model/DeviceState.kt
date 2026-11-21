package studio.mialdea.floorday.core.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

/**
 * State bound to THIS device (DataStore "device_state"). It never travels in the backup:
 * a missing [installId] after start means a fresh install or a restore (ADR-0014).
 */
data class DeviceState(
    val installId: Uuid? = null,
    val exactAlarmPromptShown: Boolean = false,
    val notificationDenials: Int = 0,
    val lastWatchdogRun: Instant? = null,
    /** Logical day closed by spending a freeze, not announced to the user yet. */
    val pendingFreezeNotice: LocalDate? = null,
)
