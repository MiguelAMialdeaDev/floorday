package studio.mialdea.floorday.core.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.data.db.entity.HabitWithSlots
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.testing.builder.aCompletion
import studio.mialdea.floorday.core.testing.builder.aMedication
import studio.mialdea.floorday.core.testing.time.TestDates

class EntityMappersTest {
    private val habit = aMedication {
        slot { label = "Noche"; time = LocalTime(22, 0); amount = Quarters.HALF }
        slot { label = "Mañana"; time = LocalTime(8, 0); amount = Quarters.ONE }
    }

    @Test
    fun `un habito vuelve igual tras pasar por las entidades`() {
        val row = HabitWithSlots(habit.toEntity(), habit.slotEntities())

        assertEquals(habit, row.toDomain())
    }

    @Test
    fun `la posicion de cada momento es su indice en la lista`() {
        assertEquals(listOf(0, 1), habit.slotEntities().map { it.position })
    }

    @Test
    fun `los momentos se leen por posicion aunque lleguen desordenados`() {
        val row = HabitWithSlots(habit.toEntity(), habit.slotEntities().reversed())

        assertEquals(habit.slots, row.toDomain().slots)
    }

    @Test
    fun `una marca vuelve igual tras pasar por la entidad`() {
        val completion = aCompletion(habit.slots.first(), TestDates.SPRINT0_END)

        assertEquals(completion, completion.toEntity().toDomain())
    }
}
