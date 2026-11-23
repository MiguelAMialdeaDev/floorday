package studio.mialdea.floorday.core.domain.usecase

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitInput
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.domain.model.SaveHabitResult
import studio.mialdea.floorday.core.domain.model.SlotInput
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aHabit
import studio.mialdea.floorday.core.testing.builder.aMedication
import studio.mialdea.floorday.core.testing.builder.habitIdOf
import studio.mialdea.floorday.core.testing.builder.slotIdOf
import studio.mialdea.floorday.core.testing.builder.uuidOf
import studio.mialdea.floorday.core.testing.repository.InMemoryCompletionRepository
import studio.mialdea.floorday.core.testing.repository.InMemoryFloordayStore
import studio.mialdea.floorday.core.testing.repository.InMemoryHabitRepository
import studio.mialdea.floorday.core.testing.time.FakeClock
import studio.mialdea.floorday.core.testing.time.FakeLogicalDayProvider

class SaveHabitUseCaseTest {

    private val today = LocalDate(2026, 12, 1)
    private val clock = FakeClock()
    private val store = InMemoryFloordayStore()
    private val habits = InMemoryHabitRepository(store)
    private val completions = InMemoryCompletionRepository(store)
    private var nextId = 100
    private val save = SaveHabitUseCase(habits, clock, FakeLogicalDayProvider(today), IdGenerator { uuidOf(nextId++) })
    private val delete = DeleteHabitUseCase(habits)

    private fun slotInput(
        id: Int? = null,
        label: String = "",
        time: LocalTime? = null,
        amount: Quarters = Quarters.ONE,
        reminder: ReminderMode = ReminderMode.NONE,
    ) = SlotInput(id?.let(::slotIdOf), label, time, amount, reminder)

    private fun input(
        from: Habit? = null,
        name: String = from?.name ?: "Leer",
        kind: HabitKind = from?.kind ?: HabitKind.CHECK,
        unitLabel: String? = from?.unitLabel,
        countsForMinimum: Boolean = from?.countsForMinimum ?: true,
        slots: List<SlotInput> = from?.currentSlots?.map { SlotInput(it.id, it.label, it.time, it.amount, it.reminder) }
            ?: listOf(slotInput()),
    ) = HabitInput(from?.id, name, kind, unitLabel, DaysOfWeek.EVERY_DAY, countsForMinimum, slots)

    private suspend fun saved(input: HabitInput): Habit = assertIs<SaveHabitResult.Saved>(save(input)).habit

    @Test
    fun `un habito nuevo recibe ids fechas y orden y cuenta para el minimo desde hoy`() = runTest {
        habits.seed(aHabit { sortOrder = 3 })

        val habit = saved(input(name = "  Leer ", unitLabel = "páginas"))

        assertEquals(uuidOf(100), habit.id.value)
        assertEquals(uuidOf(101), habit.slots.single().id.value)
        assertEquals("Leer", habit.name)
        assertNull(habit.unitLabel, "un check no lleva unidad")
        assertEquals(4, habit.sortOrder)
        assertEquals(today, habit.minimumSince)
        assertNull(habit.slots.single().addedOn, "en un habito nuevo el momento vale desde siempre")
        assertEquals(clock.now() to clock.now(), habit.createdAt to habit.updatedAt)
        habits.observeHabits().test { assertEquals(2, awaitItem().size) }
    }

    @Test
    fun `editar conserva la fecha del minimo la creacion y el orden`() = runTest {
        val stored = aHabit { id = habitIdOf(1); sortOrder = 2 }
        habits.seed(stored)
        clock.advanceBy(2.hours)

        val habit = saved(input(from = stored, name = "Leer 10 páginas"))

        assertEquals("Leer 10 páginas", habit.name)
        assertEquals(stored.minimumSince, habit.minimumSince)
        assertEquals(stored.createdAt, habit.createdAt)
        assertEquals(clock.now(), habit.updatedAt)
        assertEquals(2, habit.sortOrder)
        assertEquals(habit, habits.getHabit(stored.id))
    }

    @Test
    fun `salir del minimo lo deja a null y volver a entrar cuenta desde hoy`() = runTest {
        val stored = aHabit { id = habitIdOf(1) }
        habits.seed(stored)

        val out = saved(input(from = stored, countsForMinimum = false))
        assertNull(out.minimumSince)

        val back = saved(input(from = out, countsForMinimum = true))
        assertEquals(today, back.minimumSince)
    }

    @Test
    fun `quitar un momento con marcas lo retira desde hoy y conserva sus marcas`() = runTest {
        val stored = aMedication {
            id = habitIdOf(1)
            slot { id = slotIdOf(1); label = "Mañana"; time = LocalTime(8, 0) }
            slot { id = slotIdOf(2); label = "Noche"; time = LocalTime(22, 0); amount = Quarters.HALF }
        }
        val yesterday = LocalDate(2026, 11, 30)
        val mark = aCompletion(stored.slots[1], yesterday)
        habits.seed(stored)
        completions.seed(mark)

        val habit = saved(input(from = stored, slots = listOf(slotInput(id = 1, label = "Mañana", time = LocalTime(8, 0)))))

        assertEquals(listOf(slotIdOf(1)), habit.currentSlots.map { it.id })
        assertEquals(today, habit.slot(slotIdOf(2))?.removedOn)
        assertEquals(2, habit.slotsOn(yesterday).size, "ayer sigue teniendo las dos tomas")
        completions.observeDay(yesterday).test { assertEquals(listOf(mark), awaitItem()) }
    }

    @Test
    fun `un momento nuevo en un habito existente empieza hoy`() = runTest {
        val stored = aHabit { id = habitIdOf(1); slot { id = slotIdOf(1) } }
        habits.seed(stored)

        val habit = saved(input(from = stored, slots = listOf(slotInput(id = 1, label = "Mañana"), slotInput(label = "Noche"))))

        val added = habit.currentSlots[1]
        assertEquals("Noche", added.label)
        assertEquals(today, added.addedOn)
        assertEquals(1, habit.slotsOn(LocalDate(2026, 11, 30)).size, "ayer no tenia el momento nuevo")
    }

    @Test
    fun `pasar de medicacion a check ajusta tambien los momentos retirados`() = runTest {
        val stored = aMedication {
            id = habitIdOf(1)
            slot { id = slotIdOf(1); time = LocalTime(8, 0) }
            slot { id = slotIdOf(2); label = "Noche"; time = LocalTime(22, 0); amount = Quarters.HALF; reminder = ReminderMode.INSISTENT }
        }
        habits.seed(stored)

        val habit = saved(input(from = stored, kind = HabitKind.CHECK, slots = listOf(slotInput(id = 1, label = "Mañana"))))

        assertEquals(HabitKind.CHECK, habit.kind)
        assertNull(habit.unitLabel)
        val ended = requireNotNull(habit.slot(slotIdOf(2)))
        assertEquals(Quarters.ONE to ReminderMode.NONE, ended.amount to ended.reminder)
    }

    @Test
    fun `un habito borrado mientras se editaba no se vuelve a crear`() = runTest {
        val stored = aHabit { id = habitIdOf(1) }

        assertEquals(SaveHabitResult.NotFound, save(input(from = stored)))
        assertNull(habits.getHabit(stored.id))
    }

    @Test
    fun `un momento que no es del habito falla sin guardar nada`() = runTest {
        val stored = aHabit { id = habitIdOf(1); name = "Leer" }
        habits.seed(stored)

        assertFailsWith<IllegalArgumentException> { save(input(from = stored, name = "Otro", slots = listOf(slotInput(id = 9)))) }
        assertEquals("Leer", habits.getHabit(stored.id)?.name)
    }

    @Test
    fun `borrar se lleva el habito y su historial`() = runTest {
        val stored = aHabit { id = habitIdOf(1) }
        habits.seed(stored)
        completions.seed(aCompletion(stored.slots[0], today))

        delete(stored.id)

        assertNull(habits.getHabit(stored.id))
        completions.observeDay(today).test { assertEquals(emptyList(), awaitItem()) }
    }
}
