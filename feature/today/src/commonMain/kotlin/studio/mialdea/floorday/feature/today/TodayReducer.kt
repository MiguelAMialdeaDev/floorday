package studio.mialdea.floorday.feature.today

internal object TodayReducer {
    fun reduce(state: TodayState, event: TodayEvent): TodayState = when (event) {
        is TodayEvent.ContentLoaded -> state.copy(
            isLoading = false,
            content = event.content,
            // An undo from yesterday must not touch the new day.
            pendingUndo = state.pendingUndo?.takeIf { it.day == event.content.day },
        )

        is TodayEvent.RowStateChanged -> {
            val content = state.content
            if (content?.findRow(event.slotId) == null) state else state.copy(content = content.withState(event.slotId, event.state))
        }

        is TodayEvent.UndoArmed -> state.copy(pendingUndo = event.token)

        is TodayEvent.UndoCleared -> if (state.pendingUndo?.id == event.undoId) state.copy(pendingUndo = null) else state

        TodayEvent.OthersToggled -> state.copy(othersExpanded = !state.othersExpanded)
    }
}
