package studio.mialdea.floorday.feature.today

import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import studio.mialdea.floorday.core.designsystem.component.CheckState
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.TodayEntry
import studio.mialdea.floorday.core.domain.model.TodaySnapshot
import studio.mialdea.floorday.core.domain.time.DayStart

/** Domain snapshot to screen content: grouping, order and texts. Pure. */
internal object TodayMapper {
    private const val MINUTES_PER_HOUR = 60
    private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
    private const val NOON = 12 * MINUTES_PER_HOUR
    private const val EVENING = 20 * MINUTES_PER_HOUR
    private const val SEPARATOR = " · "

    fun map(snapshot: TodaySnapshot, dayStart: DayStart, zone: TimeZone, formatTime: (LocalTime) -> String): TodayContent {
        val start = dayStart.minutesOfDay
        val order = compareBy<TodayEntry>({ minutesFromStart(it.slot.time, start) }, { it.habit.sortOrder }, { it.habit.name })
        // A dose skipped on purpose leaves the required set but is still part of the minimum on screen.
        val (minimum, others) = snapshot.entries.partition { it.required || it.slot.id in snapshot.minimum.skippedSlotIds }
        val texts = RowTexts(snapshot, zone, formatTime)
        val byPart = minimum.groupBy { dayPartOf(it.slot.time, start) }
        return TodayContent(
            day = snapshot.day,
            sections = DayPart.entries.mapNotNull { part ->
                byPart[part]?.let { entries ->
                    TodaySection(part, entries.sortedWith(order).map { texts.row(it, partOfMinimum = true) })
                }
            },
            others = others.sortedWith(order).map { texts.row(it, partOfMinimum = false) },
            hasHabits = snapshot.hasHabits,
        )
    }

    /** Times before the day start belong to the night of the logical day (01:00 is still "tonight"). */
    fun dayPartOf(time: LocalTime?, startMinutes: Int): DayPart {
        if (time == null) return DayPart.ANYTIME
        val minutes = time.hour * MINUTES_PER_HOUR + time.minute
        return when {
            minutes < startMinutes -> DayPart.NIGHT
            minutes < NOON -> DayPart.MORNING
            minutes < EVENING -> DayPart.AFTERNOON
            else -> DayPart.NIGHT
        }
    }

    /** Slots without a time go last. */
    private fun minutesFromStart(time: LocalTime?, startMinutes: Int): Int {
        if (time == null) return Int.MAX_VALUE
        val minutes = time.hour * MINUTES_PER_HOUR + time.minute
        return (minutes - startMinutes + MINUTES_PER_DAY) % MINUTES_PER_DAY
    }

    private class RowTexts(
        private val snapshot: TodaySnapshot,
        private val zone: TimeZone,
        private val formatTime: (LocalTime) -> String,
    ) {
        fun row(entry: TodayEntry, partOfMinimum: Boolean): TodayRow = with(entry) {
            // The slot label only tells slots apart: a habit with a single slot does not need it.
            val label = slot.label.takeIf { it.isNotBlank() && habit.slotsOn(snapshot.day).size > 1 }
            val amount = if (habit.isMedication) listOfNotNull(quarterText(slot.amount), habit.unitLabel).joinToString(" ") else null
            val done = completion?.takeIf { it.status == CompletionStatus.DONE }
            TodayRow(
                slotId = slot.id,
                habitId = habit.id,
                title = habit.name,
                supportingText = listOfNotNull(label, slot.time?.let(formatTime), amount).joinToString(SEPARATOR).ifEmpty { null },
                doneTime = done?.recordedAt?.toLocalDateTime(zone)?.time?.let { formatTime(LocalTime(it.hour, it.minute)) },
                state = when (completion?.status) {
                    CompletionStatus.DONE -> CheckState.Done
                    CompletionStatus.SKIPPED -> CheckState.Skipped
                    null -> CheckState.Pending
                },
                partOfMinimum = partOfMinimum,
            )
        }
    }
}

/** Quarters to text without String.format (not in common code): 2 = "½", 6 = "1½", 4 = "1". */
internal fun quarterText(amount: Quarters): String {
    val fraction = FRACTIONS[amount.fraction.quarters]
    return if (amount.wholeUnits == 0) fraction else "${amount.wholeUnits}$fraction"
}

/** Index = remaining quarters. A list instead of a when keeps detekt's MagicNumber quiet. */
private val FRACTIONS = listOf("", "¼", "½", "¾")
