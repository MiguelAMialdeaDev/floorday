package studio.mialdea.floorday.core.designsystem.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI base (ADR-0003). S = immutable state, I = intent, E = one-off effect.
 *
 * - Intents are handled IN ORDER, one after another (an unlimited queue and a single loop):
 *   a ToggleSlot that suspends on Room followed by a quick Undo is never reversed.
 * - Do not observe long Flows inside handle, they would block the queue. Launch them in the
 *   init of the concrete ViewModel with viewModelScope.launch and apply changes with reduce.
 * - Effects wait in a buffer until the UI collects them (CollectEffects). One consumer only.
 * - An uncaught exception in handle stops the loop and the app: fail fast on purpose.
 *   Expected errors become state or an effect inside the concrete ViewModel.
 */
abstract class MviViewModel<S : Any, I : Any, E : Any>(initialState: S) : ViewModel() {

    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val effectChannel = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = effectChannel.receiveAsFlow()

    private val intents = Channel<I>(Channel.UNLIMITED)

    protected val currentState: S
        get() = mutableState.value

    init {
        // The loop starts here but only calls handle when the first intent arrives, so it never
        // touches subclass properties before they are initialised.
        viewModelScope.launch {
            for (intent in intents) {
                handle(intent)
            }
        }
    }

    fun onIntent(intent: I) {
        intents.trySend(intent)
    }

    protected abstract suspend fun handle(intent: I)

    protected fun reduce(block: S.() -> S) {
        mutableState.update(block)
    }

    protected suspend fun emit(effect: E) {
        effectChannel.send(effect)
    }
}
