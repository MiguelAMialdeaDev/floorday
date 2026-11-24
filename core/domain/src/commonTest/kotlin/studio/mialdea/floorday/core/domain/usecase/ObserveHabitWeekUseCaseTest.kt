package studio.mialdea.floorday.core.domain.usecase

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.history.HabitDayMark.DONE
import studio.mialdea.floorday.core.domain.history.HabitDayMark.MISSED
import studio.mialdea.floorday.core.domain.history.HabitDayMark.OPEN
import studio.mialdea.floorday.core.domain.history.HabitDayMark.REST
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.repository.InMemoryCompletionRepository
import studio.mialdea.floorday.core.testing.repository.InMemoryFloordayStore
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository
import studio.mialdea.floorday.core.testing.time.FakeLogicalDayProvider
import studio.mialdea.floorday.core.testing.time.FakeTimeZoneProvider
import studio.mialdea.floorday.core.testing.time.TestZones

class ObserveHabitWeekUseCaseTest {

    private val today = LocalDate(2026, 12, 7)
    private val store = InMemoryFloordayStore()
    private val habits = InMemoryHabitRepository(store)
    private val completions = InMemoryCompletionRepository(store)
    private val days = FakeLogicalDayProvider(today)
    private val observeWeek = ObserveHabitWeekUseCase(habits, completions, days, FakeTimeZoneProvider(TestZones.MADRID))

    @Test
    fun `el primer dia sale de createdAt en el dia logico de Madrid`() = runTest {
        // 02:30 in Madrid on the 5th is still the logical day of the 4th (it starts at 04:00).
        habits.seed(aHabit { createdAt = Instant.parse("2026-12-05T01:30:00Z") })

        observeWeek().test {
            assertEquals(listOf(REST, REST, REST, MISSED, MISSED, MISSED, OPEN), awaitItem().rows.single().days.map { it.mark })
        }
    }

    @Test
    fun `una marca nueva actualiza la cuadricula`() = runTest {
        val habit = aHabit()
        habits.seed(habit)

        observeWeek().test {
            assertEquals(OPEN, awaitItem().rows.single().days.last().mark)
            completions.upsert(aCompletion(habit.slots.single(), today))
            assertEquals(DONE, awaitItem().rows.single().days.last().mark)
        }
    }

    @Test
    fun `al cambiar de dia la cuadricula avanza y hoy pasa a ser ayer`() = runTest {
        val habit = aHabit()
        habits.seed(habit)
        completions.seed(aCompletion(habit.slots.single(), today))

        observeWeek().test {
            assertEquals(today, awaitItem().today)
            days.current.value = LocalDate(2026, 12, 8)
            val next = awaitItem()
            assertEquals(LocalDate(2026, 12, 8), next.days.last())
            assertEquals(listOf(DONE, OPEN), next.rows.single().days.takeLast(2).map { it.mark })
        }
    }
}
