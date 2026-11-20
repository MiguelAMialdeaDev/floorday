package studio.mialdea.floorday.core.testing.builder

import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode

/**
 * Un Día Mínimo de 4 momentos (incluye "1 + ½ = dos momentos") más un hábito fuera del mínimo.
 * Datos ficticios: nunca fármacos reales ni datos personales, el repo puede ser público.
 */
fun sampleMinimumDay(): List<Habit> = listOf(
    aMedication {
        sortOrder = 0
        slot {
            label = "Mañana"
            time = LocalTime(8, 0)
            amount = Quarters.ONE
            reminder = ReminderMode.INSISTENT
        }
        slot {
            label = "Noche"
            time = LocalTime(22, 0)
            amount = Quarters.HALF
            reminder = ReminderMode.INSISTENT
        }
    },
    aHabit {
        name = "Caminar 10 minutos"
        sortOrder = 1
        slot { label = "Tarde" }
    },
    aHabit {
        name = "Leer 5 páginas"
        sortOrder = 2
        slot { label = "Noche" }
    },
    aHabit {
        name = "Estirar"
        sortOrder = 3
        minimumSince = null
    },
)
