package studio.mialdea.floorday.feature.today.editor

import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.testing.builder.aMedication
import studio.mialdea.floorday.core.testing.builder.habitIdOf
import studio.mialdea.floorday.core.testing.builder.slotIdOf

/** Medication with two slots: morning 08:00 insistent, and night ½ with no reminder. */
internal fun sampleHabit(): Habit = aMedication {
    id = habitIdOf(1)
    slot { id = slotIdOf(1); label = "Mañana"; time = LocalTime(8, 0); reminder = ReminderMode.INSISTENT }
    slot { id = slotIdOf(2); label = "Noche"; time = LocalTime(22, 0); amount = Quarters.HALF }
}
