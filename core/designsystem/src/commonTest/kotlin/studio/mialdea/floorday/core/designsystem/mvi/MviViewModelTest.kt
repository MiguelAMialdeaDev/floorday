@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.mialdea.floorday.core.designsystem.mvi

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import studio.mialdea.floorday.core.testing.coroutines.runMainTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MviViewModelTest {

    private sealed interface TestIntent {
        data object Increment : TestIntent
        data object Ping : TestIntent
        data class Slow(val millis: Long) : TestIntent
    }

    private data class TestState(val count: Int = 0, val log: List<String> = emptyList())

    private sealed interface TestEffect {
        data object Pong : TestEffect
    }

    private class TestViewModel : MviViewModel<TestState, TestIntent, TestEffect>(TestState()) {
        override suspend fun handle(intent: TestIntent) {
            when (intent) {
                TestIntent.Increment -> reduce { copy(count = count + 1, log = log + "inc") }
                TestIntent.Ping -> emit(TestEffect.Pong)
                is TestIntent.Slow -> {
                    delay(intent.millis)
                    reduce { copy(log = log + "slow") }
                }
            }
        }

        fun snapshot(): TestState = currentState
    }

    @Test
    fun `emite el estado inicial y despues el reducido`() = runMainTest {
        val viewModel = TestViewModel()

        viewModel.state.test {
            assertEquals(0, awaitItem().count)
            viewModel.onIntent(TestIntent.Increment)
            runCurrent()

            assertEquals(1, awaitItem().count)
        }
    }

    @Test
    fun `los intents se procesan en orden aunque el primero suspenda`() = runMainTest {
        val viewModel = TestViewModel()

        viewModel.onIntent(TestIntent.Slow(millis = 100))
        viewModel.onIntent(TestIntent.Increment)
        advanceUntilIdle()

        assertEquals(listOf("slow", "inc"), viewModel.snapshot().log)
    }

    @Test
    fun `los efectos esperan en el buffer hasta que alguien los recoge`() = runMainTest {
        val viewModel = TestViewModel()

        viewModel.onIntent(TestIntent.Ping)
        advanceUntilIdle()

        viewModel.effects.test {
            assertEquals(TestEffect.Pong, awaitItem())
        }
    }
}
