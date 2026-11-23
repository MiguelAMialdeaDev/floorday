package studio.mialdea.floorday.feature.today

import androidx.lifecycle.viewModelScope
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.designsystem.component.CheckState
import studio.mialdea.floorday.core.designsystem.mvi.MviViewModel
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionKey
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.SlotId
import studio.mialdea.floorday.core.domain.model.TodayEntry
import studio.mialdea.floorday.core.domain.platform.TimeOfDayFormatter
import studio.mialdea.floorday.core.domain.time.DayStart
import studio.mialdea.floorday.core.domain.time.LogicalDayProvider
import studio.mialdea.floorday.core.domain.time.TimeZoneProvider
import studio.mialdea.floorday.core.domain.usecase.ObserveTodayUseCase
import studio.mialdea.floorday.core.domain.usecase.RestoreCompletionUseCase
import studio.mialdea.floorday.core.domain.usecase.ToggleSlotUseCase

/**
 * Today. A tap is optimistic: the row changes at once and goes back with an error message if the
 * write fails. Writes go one at a time (the intent queue and [writeMutex]), so a quick undo after
 * a tap never runs before it.
 */
class TodayViewModel(
    observeToday: ObserveTodayUseCase,
    private val toggleSlot: ToggleSlotUseCase,
    private val restoreCompletion: RestoreCompletionUseCase,
    private val logicalDay: LogicalDayProvider,
    private val timeZones: TimeZoneProvider,
    private val timeFormatter: TimeOfDayFormatter,
) : MviViewModel<TodayState, TodayIntent, TodayEffect>(TodayState()) {

    private val writeMutex = Mutex()
    private var undoSeq = 0L

    /** Latest domain rows by slot, patched after each write so a second quick tap sees the first. */
    private var entries: Map<SlotId, TodayEntry> = emptyMap()

    /** Day [entries] belong to. A write that finishes after the day changed must not patch them. */
    private var entriesDay: LocalDate? = null

    /**
     * Rows with a write in flight and the state they show. Room may still emit the previous
     * write's result meanwhile; without this a quick second tap would flicker back.
     */
    private val inFlight = mutableMapOf<CompletionKey, CheckState>()

    init {
        viewModelScope.launch {
            observeToday().collect { snapshot ->
                entries = snapshot.entries.associateBy { it.slot.id }
                entriesDay = snapshot.day
                // DayStart stays fixed until the setting arrives (S1).
                val mapped = TodayMapper.map(snapshot, DayStart.Default, timeZones.current(), timeFormatter::format)
                val content = inFlight.entries
                    .filter { (key, _) -> key.day == snapshot.day }
                    .fold(mapped) { acc, (key, state) -> acc.withState(key.slotId, state) }
                dispatch(TodayEvent.ContentLoaded(content))
            }
        }
    }

    override suspend fun handle(intent: TodayIntent) {
        when (intent) {
            is TodayIntent.ToggleSlot -> toggle(intent.slotId)
            is TodayIntent.Undo -> undo(intent.undoId)
            is TodayIntent.UndoDismissed -> dispatch(TodayEvent.UndoCleared(intent.undoId))
            TodayIntent.ToggleOthers -> dispatch(TodayEvent.OthersToggled)
            TodayIntent.Resumed -> logicalDay.refresh()
        }
    }

    // Any write failure puts the row back and tells the user; CancellationException is rethrown first.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun toggle(slotId: SlotId) {
        val content = currentState.content ?: return
        val row = content.findRow(slotId) ?: return
        val entry = entries[slotId] ?: return
        val key = CompletionKey(slotId, content.day)
        optimistic(key, if (row.state == CheckState.Done) CheckState.Pending else CheckState.Done)
        try {
            val outcome = writeMutex.withLock { toggleSlot(entry.habit, slotId, content.day, entry.completion) }
            // If the day rolled over during the write, the new day's rows are already in place:
            // patching them or arming an undo with yesterday's mark would corrupt today.
            if (entriesDay != content.day) return
            entries = entries + (slotId to entry.copy(completion = outcome.current))
            val token = UndoToken(++undoSeq, slotId, content.day, outcome.previous)
            dispatch(TodayEvent.UndoArmed(token))
            emit(TodayEffect.ShowUndo(token.id, row.title, markedDone = outcome.current != null))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            if (currentState.content?.day == content.day) dispatch(TodayEvent.RowStateChanged(slotId, row.state))
            emit(TodayEffect.ShowSaveError)
        } finally {
            inFlight.remove(key)
        }
    }

    private fun optimistic(key: CompletionKey, state: CheckState) {
        inFlight[key] = state
        dispatch(TodayEvent.RowStateChanged(key.slotId, state))
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun undo(undoId: Long) {
        val token = currentState.pendingUndo?.takeIf { it.id == undoId } ?: return
        dispatch(TodayEvent.UndoCleared(undoId))
        val before = currentState.content?.takeIf { it.day == token.day }?.findRow(token.slotId)?.state
        val key = CompletionKey(token.slotId, token.day)
        if (before != null) optimistic(key, token.previous.toCheckState())
        try {
            writeMutex.withLock { restoreCompletion(token.slotId, token.day, token.previous) }
            if (entriesDay == token.day) {
                entries[token.slotId]?.let { entries = entries + (token.slotId to it.copy(completion = token.previous)) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            if (before != null && currentState.content?.day == token.day) dispatch(TodayEvent.RowStateChanged(token.slotId, before))
            emit(TodayEffect.ShowSaveError)
        } finally {
            inFlight.remove(key)
        }
    }

    private fun dispatch(event: TodayEvent) = reduce { TodayReducer.reduce(this, event) }
}

private fun Completion?.toCheckState(): CheckState = when (this?.status) {
    CompletionStatus.DONE -> CheckState.Done
    CompletionStatus.SKIPPED -> CheckState.Skipped
    null -> CheckState.Pending
}
