package studio.mialdea.floorday.feature.today

import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import studio.mialdea.floorday.feature.today.resources.Res
import studio.mialdea.floorday.feature.today.resources.today_title
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TodayStringsTest {

    @Test
    fun `el titulo se lee de compose resources en ingles`() = runTest {
        assertEquals("Today", getString(Res.string.today_title))
    }

    @Test
    @Config(sdk = [36], qualifiers = "es")
    fun `el titulo se lee en espanol con el locale es`() = runTest {
        assertEquals("Hoy", getString(Res.string.today_title))
    }
}
