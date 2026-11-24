package studio.mialdea.floorday.feature.history

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.history.HabitDay
import studio.mialdea.floorday.core.domain.history.HabitDayMark.DONE
import studio.mialdea.floorday.core.domain.history.HabitDayMark.OPEN
import studio.mialdea.floorday.core.domain.history.HabitWeek
import studio.mialdea.floorday.core.domain.history.HabitWeekRow
import studio.mialdea.floorday.core.testing.builder.aHabit

class HistoryReducerTest {

    private val yesterday = LocalDate(2026, 12, 6)
    private val today = LocalDate(2026, 12, 7)

    @Test
    fun `la semana pasa a filas con el nombre y las marcas en orden`() {
        val habit = aHabit { name = "Leer" }
        val week = HabitWeek(
            today = today,
            days = listOf(yesterday, today),
            rows = listOf(HabitWeekRow(habit, listOf(HabitDay(yesterday, DONE), HabitDay(today, OPEN)))),
        )

        val state = HistoryReducer.reduce(HistoryState(), week)

        assertFalse(state.loading)
        assertEquals(listOf(yesterday, today), state.days)
        assertEquals(listOf(HistoryRow(habit.id, "Leer", listOf(DONE, OPEN))), state.rows)
    }

    @Test
    fun `sin habitos la pantalla esta vacia y mientras carga no`() {
        val loaded = HistoryReducer.reduce(HistoryState(), HabitWeek(today, listOf(today), emptyList()))

        assertTrue(loaded.isEmpty)
        assertFalse(HistoryState().isEmpty)
    }
}
