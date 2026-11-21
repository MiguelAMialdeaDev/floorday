package studio.mialdea.floorday.core.data.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import studio.mialdea.floorday.core.domain.model.HabitKind

class FloordayConvertersTest {
    private val converters = FloordayConverters

    @Test
    fun `un uuid se guarda como texto en minusculas y vuelve igual`() {
        val id = Uuid.parse("0B4F1F9E-6C1A-4C2E-9A51-1F0E2D3C4B5A")

        val text = converters.uuidToText(id)

        assertEquals("0b4f1f9e-6c1a-4c2e-9a51-1f0e2d3c4b5a", text)
        assertEquals(id, converters.textToUuid(text))
    }

    @Test
    fun `una fecha se guarda en ISO y vuelve igual`() {
        val day = LocalDate(2027, 3, 28)

        val text = converters.localDateToText(day)

        assertEquals("2027-03-28", text)
        assertEquals(day, converters.textToLocalDate(text))
    }

    @Test
    fun `las fechas en ISO se ordenan como texto igual que en el calendario`() {
        val days = listOf(LocalDate(2027, 1, 2), LocalDate(2026, 12, 31), LocalDate(2027, 10, 1))

        val sortedAsText = days.mapNotNull(converters::localDateToText).sorted()

        assertEquals(days.sorted().map { it.toString() }, sortedAsText)
    }

    @Test
    fun `una hora se guarda en minutos del dia`() {
        assertEquals(0, converters.localTimeToMinutes(LocalTime(0, 0)))
        assertEquals(1_439, converters.localTimeToMinutes(LocalTime(23, 59)))
        assertEquals(LocalTime(22, 30), converters.minutesToLocalTime(1_350))
    }

    @Test
    fun `un instante se guarda en milisegundos epoch`() {
        val at = Instant.fromEpochMilliseconds(1_796_542_200_123)

        assertEquals(1_796_542_200_123, converters.instantToEpochMillis(at))
        assertEquals(at, converters.epochMillisToInstant(converters.instantToEpochMillis(at)))
    }

    @Test
    fun `los nulos siguen siendo nulos`() {
        assertNull(converters.uuidToText(null))
        assertNull(converters.textToUuid(null))
        assertNull(converters.localDateToText(null))
        assertNull(converters.textToLocalDate(null))
        assertNull(converters.localTimeToMinutes(null))
        assertNull(converters.minutesToLocalTime(null))
        assertNull(converters.instantToEpochMillis(null))
        assertNull(converters.epochMillisToInstant(null))
    }

    @Test
    fun `un enum se guarda por su clave y no por su nombre`() {
        assertEquals("medication", converters.habitKindToKey(HabitKind.MEDICATION))
        assertEquals(HabitKind.MEDICATION, converters.keyToHabitKind("medication"))
    }

    @Test
    fun `una clave desconocida en la base de datos falla con un mensaje claro`() {
        val error = assertFailsWith<IllegalStateException> { converters.keyToHabitKind("MEDICATION") }

        assertEquals("Unknown HabitKind key in the database: 'MEDICATION'", error.message)
    }
}
