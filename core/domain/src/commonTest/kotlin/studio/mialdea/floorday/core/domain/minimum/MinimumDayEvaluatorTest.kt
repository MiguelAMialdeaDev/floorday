package studio.mialdea.floorday.core.domain.minimum

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.domain.model.Completion
import studio.mialdea.floorday.core.domain.model.CompletionStatus
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.EntrySource
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.time.DayStart
import studio.mialdea.floorday.core.domain.time.LogicalDayCalculator
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.builder.aMedication
import studio.mialdea.floorday.core.testing.builder.slotIdOf
import studio.mialdea.floorday.core.testing.time.TestZones

private fun Habit.mark(day: LocalDate, slotIndex: Int = 0, status: CompletionStatus = CompletionStatus.DONE) =
    aCompletion(slots[slotIndex], day, status)

/** Medicación ficticia de 1 por la mañana y ½ por la noche: dos momentos. */
private fun oneAndAHalf(name: String = "Vitamina D") = aMedication {
    this.name = name
    slot { label = "Mañana" }
    slot {
        label = "Noche"
        amount = Quarters.HALF
    }
}

class MinimumDayEvaluatorTest {

    private val wednesday = LocalDate(2026, 12, 16)

    // Mínimo ficticio: una medicación de 1 + ½ y 3 checks.
    private val vitaminD = oneAndAHalf()
    private val walk = aHabit { name = "Caminar 10 minutos" }
    private val read = aHabit { name = "Leer 5 páginas" }
    private val stretch = aHabit { name = "Estirar" }
    private val sample = listOf(vitaminD, walk, read, stretch)

    private fun evaluate(completions: List<Completion>, habits: List<Habit> = sample, day: LocalDate = wednesday) =
        MinimumDayEvaluator.evaluate(day, habits, completions)

    @Test
    fun `4 elementos con medicacion de 2 momentos son 5 requeridos`() {
        val result = evaluate(emptyList())

        assertEquals(5, result.required)
        assertEquals(0, result.done)
        assertEquals(MinimumDayOutcome.NOT_STARTED, result.outcome)
        assertFalse(result.isMet)
    }

    @Test
    fun `todo hecho es MET`() {
        val all = listOf(
            vitaminD.mark(wednesday, 0),
            vitaminD.mark(wednesday, 1),
            walk.mark(wednesday),
            read.mark(wednesday),
            stretch.mark(wednesday),
        )

        val result = evaluate(all)

        assertEquals(MinimumDayOutcome.MET, result.outcome)
        assertTrue(result.isMet)
        assertEquals(5, result.done)
    }

    @Test
    fun `1 mas medio son dos momentos y solo la manana no basta`() {
        val result = evaluate(
            listOf(vitaminD.mark(wednesday, 0), walk.mark(wednesday), read.mark(wednesday), stretch.mark(wednesday)),
        )

        assertEquals(MinimumDayOutcome.PARTIAL, result.outcome)
        assertEquals(4, result.done)
        assertEquals(5, result.required)
        assertTrue(vitaminD.slots[1].id in result.requiredSlotIds)
        assertFalse(vitaminD.slots[1].id in result.doneSlotIds)
    }

    @Test
    fun `una toma omitida a proposito es neutra y el dia puede ser MET`() {
        val result = evaluate(
            listOf(
                vitaminD.mark(wednesday, 0),
                vitaminD.mark(wednesday, 1, CompletionStatus.SKIPPED),
                walk.mark(wednesday),
                read.mark(wednesday),
                stretch.mark(wednesday),
            ),
        )

        assertEquals(MinimumDayOutcome.MET, result.outcome)
        assertEquals(4, result.required)
        assertEquals(setOf(vitaminD.slots[1].id), result.skippedSlotIds)
    }

    @Test
    fun `si todas las tomas requeridas se omiten el dia es REST`() {
        val pill = aMedication {
            name = "Pastilla mañana"
            slot { label = "Mañana" }
            slot { label = "Mediodía" }
        }

        val result = evaluate(
            listOf(pill.mark(wednesday, 0, CompletionStatus.SKIPPED), pill.mark(wednesday, 1, CompletionStatus.SKIPPED)),
            habits = listOf(pill),
        )

        assertEquals(MinimumDayOutcome.REST, result.outcome)
        assertEquals(0, result.required)
    }

    @Test
    fun `SKIPPED en un check normal se ignora y sigue pendiente`() {
        val result = evaluate(listOf(walk.mark(wednesday, status = CompletionStatus.SKIPPED)))

        assertEquals(5, result.required)
        assertEquals(0, result.done)
        assertTrue(result.skippedSlotIds.isEmpty())
    }

    @Test
    fun `sin habitos el dia es REST`() {
        assertEquals(MinimumDayOutcome.REST, evaluate(emptyList(), habits = emptyList()).outcome)
    }

    @Test
    fun `un habito fuera del minimo no cuenta aunque este hecho`() {
        val outside = aHabit {
            name = "Regar plantas"
            minimumSince = null
        }

        val result = evaluate(listOf(outside.mark(wednesday)), habits = sample + outside)

        assertEquals(5, result.required)
        assertEquals(0, result.done)
    }

    @Test
    fun `un dia de la semana no activo no es requerido`() {
        val weekdays = aHabit { days = DaysOfWeek.WEEKDAYS }
        val sunday = LocalDate(2026, 12, 20)

        assertEquals(1, evaluate(emptyList(), listOf(weekdays), wednesday).required)
        assertEquals(MinimumDayOutcome.REST, evaluate(emptyList(), listOf(weekdays), sunday).outcome)
    }

    @Test
    fun `minimumSince marca desde que dia cuenta`() {
        val newHabit = aHabit { minimumSince = wednesday }

        assertEquals(0, evaluate(emptyList(), listOf(newHabit), LocalDate(2026, 12, 15)).required)
        assertEquals(1, evaluate(emptyList(), listOf(newHabit), wednesday).required)
    }

    @Test
    fun `un habito archivado deja de contar desde el dia de archivo incluido`() {
        val archived = aHabit { archivedOn = wednesday }

        assertEquals(1, evaluate(emptyList(), listOf(archived), LocalDate(2026, 12, 15)).required)
        assertEquals(0, evaluate(emptyList(), listOf(archived), wednesday).required)
    }

    @Test
    fun `las marcas de otro dia y de momentos desconocidos se ignoran`() {
        val foreign = Completion(
            slotId = slotIdOf(999),
            day = wednesday,
            status = CompletionStatus.DONE,
            recordedAt = Instant.parse("2026-12-16T08:00:00Z"),
            amount = Quarters.ONE,
            source = EntrySource.WIDGET,
        )

        val result = evaluate(listOf(walk.mark(LocalDate(2026, 12, 15)), foreign))

        assertEquals(0, result.done)
    }

    @Test
    fun `el dia de la semana es el del dia logico y no el del calendario`() {
        // Lunes 21-dic-2026 a la 01:00 en Madrid: el día lógico es el domingo 20.
        val instant = Instant.parse("2026-12-21T00:00:00Z")
        val day = LogicalDayCalculator.dayOf(instant, TestZones.MADRID, DayStart.Default)
        val mondayOnly = aHabit { days = DaysOfWeek.of(DayOfWeek.MONDAY) }

        val result = evaluate(emptyList(), listOf(mondayOnly), day)

        assertEquals(DayOfWeek.SUNDAY, day.dayOfWeek)
        assertEquals(MinimumDayOutcome.REST, result.outcome)
    }
}

class MinimumSizeTest {

    private val today = LocalDate(2026, 12, 16)

    private fun checks(n: Int) = List(n) { aHabit { name = "Hábito $it" } }

    @Test
    fun `de 3 a 5 elementos es OK`() {
        assertEquals(MinimumSizeCheck.TOO_FEW, MinimumSize.check(checks(2), today))
        assertEquals(MinimumSizeCheck.OK, MinimumSize.check(checks(3), today))
        assertEquals(MinimumSizeCheck.OK, MinimumSize.check(checks(5), today))
        assertEquals(MinimumSizeCheck.TOO_MANY, MinimumSize.check(checks(6), today))
    }

    @Test
    fun `la medicacion con dos momentos cuenta como un elemento`() {
        val habits = checks(2) + oneAndAHalf()

        assertEquals(MinimumSizeCheck.OK, MinimumSize.check(habits, today))
    }

    @Test
    fun `no cuentan los archivados ni los que estan fuera del minimo`() {
        val habits = checks(2) +
            aHabit { archivedOn = today } +
            aHabit { minimumSince = null } +
            aHabit { archivedOn = LocalDate(2026, 12, 17) }

        assertEquals(MinimumSizeCheck.OK, MinimumSize.check(habits, today))
    }
}
