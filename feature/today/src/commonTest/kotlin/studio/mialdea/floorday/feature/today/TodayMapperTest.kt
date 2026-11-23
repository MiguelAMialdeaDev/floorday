package studio.mialdea.floorday.feature.today

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.designsystem.component.CheckState
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.TodaySnapshot
import studio.mialdea.floorday.core.domain.time.DayStart
import studio.mialdea.floorday.core.domain.usecase.ObserveTodayUseCase
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.builder.aMedication
import studio.mialdea.floorday.core.testing.builder.sampleMinimumDay
import studio.mialdea.floorday.core.testing.repository.InMemoryCompletionRepository
import studio.mialdea.floorday.core.testing.repository.InMemoryFloordayStore
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository
import studio.mialdea.floorday.core.testing.time.FakeLogicalDayProvider
import studio.mialdea.floorday.core.testing.time.TestZones
import studio.mialdea.floorday.core.testing.time.instantOf

class TodayMapperTest {

    private val day = LocalDate(2026, 12, 1)

    @Test
    fun `los momentos se agrupan por parte del dia y la 01 00 sigue siendo esta noche`() = runTest {
        val content = map(
            listOf(
                aHabit { name = "Leer"; slot { time = LocalTime(1, 0) } },
                aHabit { name = "Cenar"; slot { time = LocalTime(22, 0) } },
                aHabit { name = "Agua"; slot { time = LocalTime(8, 0) } },
                aHabit { name = "Merendar"; slot { time = LocalTime(17, 0) } },
                aHabit { name = "Estirar" },
            ),
        )

        assertEquals(
            listOf(
                DayPart.MORNING to listOf("Agua"),
                DayPart.AFTERNOON to listOf("Merendar"),
                DayPart.NIGHT to listOf("Cenar", "Leer"),
                DayPart.ANYTIME to listOf("Estirar"),
            ),
            content.sections.map { section -> section.part to section.rows.map { it.title } },
        )
    }

    @Test
    fun `la etiqueta del momento solo sale si el habito tiene varios`() = runTest {
        val content = map(
            listOf(
                aMedication {
                    slot { label = "Mañana"; time = LocalTime(8, 0); amount = Quarters.ONE }
                    slot { label = "Noche"; time = LocalTime(22, 0); amount = Quarters.HALF }
                },
                aHabit { name = "Agua"; slot { label = "Mañana"; time = LocalTime(9, 0) } },
            ),
        )

        val texts = content.sections.flatMap { it.rows }.map { it.supportingText }
        assertEquals(listOf("Mañana · 08:00 · 1 pastilla", "09:00", "Noche · 22:00 · ½ pastilla"), texts)
    }

    @Test
    fun `una toma omitida sigue en el minimo y el anillo coincide con el evaluador`() = runTest {
        val medication = aMedication {
            slot { label = "Mañana"; time = LocalTime(8, 0) }
            slot { label = "Noche"; time = LocalTime(22, 0) }
        }
        val walk = aHabit { name = "Caminar"; slot { time = LocalTime(18, 0) } }
        val marks = listOf(
            aCompletion(medication.slots[0], day, status = CompletionStatus.SKIPPED),
            aCompletion(walk.slots[0], day),
        )

        val snapshot = snapshotOf(listOf(medication, walk), marks)
        val content = TodayMapper.map(snapshot, DayStart.Default, TestZones.MADRID) { it.toString() }

        assertEquals(3, content.sections.sumOf { it.rows.size })
        assertEquals(CheckState.Skipped, content.findRow(medication.slots[0].id)?.state)
        assertEquals(snapshot.minimum.done to snapshot.minimum.required, content.done to content.required)
    }

    @Test
    fun `lo que no cuenta para el minimo va plegado en otros habitos`() = runTest {
        val content = map(sampleMinimumDay())

        assertEquals(listOf("Estirar"), content.others.map { it.title })
        assertEquals(4, content.required)
    }

    @Test
    fun `una fila hecha muestra la hora de la marca`() = runTest {
        val water = aHabit { name = "Agua"; slot { time = LocalTime(8, 0) } }
        val mark = aCompletion(water.slots[0], day, recordedAt = instantOf(day, LocalTime(8, 42)))

        val row = map(listOf(water), listOf(mark)).sections.single().rows.single()

        assertEquals("08:42", row.doneTime)
        assertEquals(CheckState.Done, row.state)
    }

    @Test
    fun `una fila pendiente no tiene hora de marca`() = runTest {
        val row = map(listOf(aHabit { slot { time = LocalTime(8, 0) } })).sections.single().rows.single()

        assertNull(row.doneTime)
    }

    @Test
    fun `los cuartos se escriben como fraccion`() {
        assertEquals(listOf("¼", "½", "1", "1½", "2¾"), listOf(1, 2, 4, 6, 11).map { quarterText(Quarters(it)) })
    }

    private suspend fun map(habits: List<Habit>, marks: List<Completion> = emptyList()): TodayContent =
        TodayMapper.map(snapshotOf(habits, marks), DayStart.Default, TestZones.MADRID) { it.toString() }

    /** The real use case over in-memory repos, so the mapper gets exactly what production gives it. */
    private suspend fun snapshotOf(habits: List<Habit>, marks: List<Completion>): TodaySnapshot {
        val store = InMemoryFloordayStore()
        val habitRepository = InMemoryHabitRepository(store).apply { seed(*habits.toTypedArray()) }
        val completionRepository = InMemoryCompletionRepository(store).apply { seed(*marks.toTypedArray()) }
        return ObserveTodayUseCase(habitRepository, completionRepository, FakeLogicalDayProvider(day))().first()
    }
}
