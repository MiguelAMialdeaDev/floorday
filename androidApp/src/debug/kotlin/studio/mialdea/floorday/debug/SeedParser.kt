package studio.mialdea.floorday.debug

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import studio.mialdea.floorday.core.domain.model.DaysOfWeek
import studio.mialdea.floorday.core.domain.model.Habit
import studio.mialdea.floorday.core.domain.model.HabitKind
import studio.mialdea.floorday.core.domain.model.HabitSlot
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.model.Quarters
import studio.mialdea.floorday.core.domain.model.ReminderMode
import studio.mialdea.floorday.core.domain.model.newHabitId
import studio.mialdea.floorday.core.domain.model.newSlotId

/**
 * Reads the debug seed (format in seed.example.json). Every habit is due every day, has no
 * reminders and, if `minimum` is true, counts for the Minimum Day from [today]. The Habit
 * constructor validates the rest, so a bad seed fails loudly instead of saving half a habit.
 */
internal object SeedParser {

    fun parse(json: String, ids: IdGenerator, today: LocalDate, now: Instant): List<Habit> =
        Json.parseToJsonElement(json).jsonObject.getValue("habits").jsonArray.mapIndexed { index, element ->
            habit(element.jsonObject, index, ids, today, now)
        }

    private fun habit(item: JsonObject, index: Int, ids: IdGenerator, today: LocalDate, now: Instant): Habit {
        val id = ids.newHabitId()
        val kind = item.text("kind")?.let { key -> requireNotNull(HabitKind.entries.firstOrNull { it.key == key }) { "Unknown kind: $key" } }
            ?: HabitKind.CHECK
        val slots = item["slots"]?.jsonArray?.map { it.jsonObject }.orEmpty().ifEmpty { listOf(JsonObject(emptyMap())) }
        return Habit(
            id = id,
            name = requireNotNull(item.text("name")) { "Habit $index has no name" },
            kind = kind,
            slots = slots.map { slot ->
                HabitSlot(
                    id = ids.newSlotId(),
                    habitId = id,
                    label = slot.text("label").orEmpty(),
                    time = slot.text("time")?.let(LocalTime::parse),
                    amount = Quarters(slot["quarters"]?.jsonPrimitive?.intOrNull ?: Quarters.PER_UNIT),
                    reminder = ReminderMode.NONE,
                )
            },
            days = DaysOfWeek.EVERY_DAY,
            minimumSince = today.takeIf { item["minimum"]?.jsonPrimitive?.booleanOrNull ?: true },
            archivedOn = null,
            sortOrder = index,
            unitLabel = item.text("unit"),
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun JsonObject.text(key: String): String? = get(key)?.jsonPrimitive?.content
}
