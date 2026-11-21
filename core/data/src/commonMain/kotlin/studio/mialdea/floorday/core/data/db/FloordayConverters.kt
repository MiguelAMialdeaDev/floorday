package studio.mialdea.floorday.core.data.db

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.EntrySource
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.ReminderMode

/**
 * Column representations of schema v1. Changing any of them requires a migration.
 * - Uuid: lowercase TEXT (the built-in converter, which uses BLOB, is disabled).
 * - LocalDate: ISO `yyyy-MM-dd` TEXT, which also sorts chronologically.
 * - LocalTime: minutes of the day; seconds are not stored (slot times are HH:mm).
 * - Instant: epoch milliseconds.
 * - Enums: their stable text `key`, never `name`, so renaming a constant does not break rows.
 */
object FloordayConverters {
    private const val MINUTES_PER_HOUR = 60

    @ColumnTypeConverter
    fun uuidToText(value: Uuid?): String? = value?.toString()

    @ColumnTypeConverter
    fun textToUuid(value: String?): Uuid? = value?.let(Uuid::parse)

    @ColumnTypeConverter
    fun localDateToText(value: LocalDate?): String? = value?.toString()

    @ColumnTypeConverter
    fun textToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @ColumnTypeConverter
    fun localTimeToMinutes(value: LocalTime?): Int? = value?.let { it.hour * MINUTES_PER_HOUR + it.minute }

    @ColumnTypeConverter
    fun minutesToLocalTime(value: Int?): LocalTime? =
        value?.let { LocalTime(hour = it / MINUTES_PER_HOUR, minute = it % MINUTES_PER_HOUR) }

    @ColumnTypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilliseconds()

    @ColumnTypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::fromEpochMilliseconds)

    @ColumnTypeConverter
    fun habitKindToKey(value: HabitKind): String = value.key

    @ColumnTypeConverter
    fun keyToHabitKind(value: String): HabitKind = HabitKind.fromKey(value) ?: unknownKey("HabitKind", value)

    @ColumnTypeConverter
    fun reminderModeToKey(value: ReminderMode): String = value.key

    @ColumnTypeConverter
    fun keyToReminderMode(value: String): ReminderMode =
        ReminderMode.fromKey(value) ?: unknownKey("ReminderMode", value)

    @ColumnTypeConverter
    fun completionStatusToKey(value: CompletionStatus): String = value.key

    @ColumnTypeConverter
    fun keyToCompletionStatus(value: String): CompletionStatus =
        CompletionStatus.fromKey(value) ?: unknownKey("CompletionStatus", value)

    @ColumnTypeConverter
    fun entrySourceToKey(value: EntrySource): String = value.key

    @ColumnTypeConverter
    fun keyToEntrySource(value: String): EntrySource = EntrySource.fromKey(value) ?: unknownKey("EntrySource", value)

    private fun unknownKey(type: String, key: String): Nothing =
        throw IllegalStateException("Unknown $type key in the database: '$key'")
}
