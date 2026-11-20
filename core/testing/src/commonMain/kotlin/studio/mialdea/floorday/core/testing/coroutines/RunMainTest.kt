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
 * runTest with Dispatchers.Main replaced by a StandardTestDispatcher that shares the scheduler.
 * It is the only way to replace Main in the repo: use it in every test that touches viewModelScope.
 * Note: Main.immediate is not immediate here, so after vm.onIntent(...) call runCurrent()
 * or advanceUntilIdle(). Valid on Android host and iOS (runTest blocks on JVM and Native).
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
