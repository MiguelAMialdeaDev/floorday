@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.mialdea.floorday.core.testing.coroutines

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

val DEFAULT_TEST_TIMEOUT: Duration = 10.seconds

/**
 * runTest con Dispatchers.Main sustituido por un StandardTestDispatcher que comparte scheduler.
 * Es la única forma de sustituir Main en el repo: úsalo en todo test que toque viewModelScope.
 * Ojo: Main.immediate no es inmediato aquí, así que tras vm.onIntent(...) llama a runCurrent()
 * o advanceUntilIdle(). Válido en Android host e iOS (runTest bloquea en JVM y Native).
 */
fun runMainTest(
    timeout: Duration = DEFAULT_TEST_TIMEOUT,
    testBody: suspend TestScope.() -> Unit,
): TestResult {
    val main = StandardTestDispatcher()
    Dispatchers.setMain(main)
    try {
        return runTest(main, timeout, testBody)
    } finally {
        Dispatchers.resetMain()
    }
}
