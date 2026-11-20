package studio.mialdea.floorday.core.testing.builder

import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode

/**
 * A Minimum Day of 4 slots (includes "1 + ½ = two slots") plus a habit outside the minimum.
 * Fake data: never real drugs or personal data, the repo may be public.
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
