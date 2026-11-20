@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.mialdea.floorday.core.testing.coroutines

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle

class RunMainTestTest {

    @Test
    fun `Main usa el scheduler del test`() = runMainTest {
        var ran = false

        CoroutineScope(Dispatchers.Main).launch {
            delay(1.seconds)
            ran = true
        }

        assertFalse(ran)
        advanceUntilIdle()
        assertTrue(ran)
    }
}
