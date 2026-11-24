package studio.mialdea.floorday.core.domain.history

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import studio.mialdea.floorday.core.domain.history.HabitDayMark.DONE
import studio.mialdea.floorday.core.domain.history.HabitDayMark.MISSED
import studio.mialdea.floorday.core.domain.history.HabitDayMark.OPEN
import studio.mialdea.floorday.core.domain.history.HabitDayMark.REST
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.builder.aMedication

class HabitWeekCalculatorTest {

    // Monday 7-Dec-2026; the grid goes from Tuesday 1 to Monday 7.
    private val today = LocalDate(2026, 12, 7)
    private val firstOfWeek = LocalDate(2026, 12, 1)

    private fun week(habits: List<Habit>, marks: List<Completion> = emptyList(), firstDay: LocalDate = firstOfWeek) =
        HabitWeekCalculator.compute(today, habits, marks, firstDayOf = { firstDay })

    private fun daysAgo(n: Int) = today.minus(n, DateTimeUnit.DAY)

    @Test
    fun `la cuadricula tiene siete dias del mas antiguo a hoy`() {
        val grid = week(listOf(aHabit()))

        assertEquals((6 downTo 0).map(::daysAgo), grid.days)
        assertEquals(grid.days, grid.rows.single().days.map { it.day })
    }

    @Test
    fun `un dia pasado hecho es DONE y uno sin hacer es MISSED`() {
        val habit = aHabit()
        val slot = habit.slots.single()

        val grid = week(listOf(habit), listOf(aCompletion(slot, daysAgo(1))))

        assertEquals(listOf(MISSED, MISSED, MISSED, MISSED, MISSED, DONE, OPEN), grid.rows.single().days.map { it.mark })
    }

    @Test
    fun `hoy hecho es DONE y sin hacer es OPEN`() {
        val habit = aHabit()

        val done = week(listOf(habit), listOf(aCompletion(habit.slots.single(), today)))
        val open = week(listOf(habit))

        assertEquals(DONE, done.rows.single().days.last().mark)
        assertEquals(OPEN, open.rows.single().days.last().mark)
    }

    @Test
    fun `los dias antes de crear el habito son REST`() {
        val grid = week(listOf(aHabit()), firstDay = daysAgo(2))

        assertEquals(listOf(REST, REST, REST, REST, MISSED, MISSED, OPEN), grid.rows.single().days.map { it.mark })
    }

    @Test
    fun `un dia de la semana que no toca es REST`() {
        val weekdays = DaysOfWeek.EVERY_DAY.toggle(DayOfWeek.SATURDAY).toggle(DayOfWeek.SUNDAY)

        val grid = week(listOf(aHabit { days = weekdays }))

        // Saturday 5 and Sunday 6 are the 5th and 6th columns.
        assertEquals(REST, grid.rows.single().days[4].mark)
        assertEquals(REST, grid.rows.single().days[5].mark)
        assertEquals(MISSED, grid.rows.single().days[3].mark)
    }

    @Test
    fun `una medicacion solo es DONE con todas sus tomas`() {
        val medication = aMedication { slot { label = "Mañana" }; slot { label = "Noche" } }
        val (morning, night) = medication.slots

        val grid = week(
            listOf(medication),
            listOf(aCompletion(morning, daysAgo(2)), aCompletion(morning, daysAgo(1)), aCompletion(night, daysAgo(1))),
        )

        assertEquals(MISSED, grid.rows.single().days[4].mark)
        assertEquals(DONE, grid.rows.single().days[5].mark)
    }

    @Test
    fun `una toma omitida a proposito no cuenta y si se omiten todas el dia es REST`() {
        val medication = aMedication { slot { label = "Mañana" }; slot { label = "Noche" } }
        val (morning, night) = medication.slots

        val grid = week(
            listOf(medication),
            listOf(
                aCompletion(morning, daysAgo(2)),
                aCompletion(night, daysAgo(2), CompletionStatus.SKIPPED),
                aCompletion(morning, daysAgo(1), CompletionStatus.SKIPPED),
                aCompletion(night, daysAgo(1), CompletionStatus.SKIPPED),
            ),
        )

        assertEquals(DONE, grid.rows.single().days[4].mark)
        assertEquals(REST, grid.rows.single().days[5].mark)
    }

    @Test
    fun `un momento anadido despues no cambia los dias anteriores`() {
        val habit = aHabit { slot { label = "Mañana" }; slot { label = "Noche"; addedOn = today } }
        val (morning, _) = habit.slots

        val grid = week(listOf(habit), listOf(aCompletion(morning, daysAgo(1))))

        assertEquals(DONE, grid.rows.single().days[5].mark)
        assertEquals(OPEN, grid.rows.single().days[6].mark)
    }

    @Test
    fun `un habito archivado antes de hoy no sale y uno que se archiva manana si`() {
        val gone = aHabit { name = "Viejo"; archivedOn = today }
        val leaving = aHabit { name = "Se va"; archivedOn = today.plus(1, DateTimeUnit.DAY) }

        val grid = week(listOf(gone, leaving))

        assertEquals(listOf("Se va"), grid.rows.map { it.habit.name })
    }

    @Test
    fun `las filas siguen el orden de los habitos`() {
        val grid = week(listOf(aHabit { name = "B" }, aHabit { name = "A" }))

        assertEquals(listOf("B", "A"), grid.rows.map { it.habit.name })
    }
}
