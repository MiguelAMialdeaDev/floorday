package studio.mialdea.floorday.feature.today

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import studio.mialdea.floorday.core.designsystem.component.CheckState
import studio.mialdea.floorday.core.testing.builder.slotIdOf

class TodayReducerTest {

    private val day = LocalDate(2026, 12, 1)
    private val pill = row(1, "Pastilla")
    private val walk = row(2, "Caminar")
    private val content = TodayContent(
        day = day,
        sections = listOf(TodaySection(DayPart.MORNING, listOf(pill, walk))),
        others = listOf(row(3, "Estirar", partOfMinimum = false)),
        hasHabits = true,
    )
    private val loaded = TodayState(isLoading = false, content = content)

    @Test
    fun `el contenido cargado quita la carga`() {
        val state = TodayReducer.reduce(TodayState(), TodayEvent.ContentLoaded(content))

        assertFalse(state.isLoading)
        assertEquals(content, state.content)
    }

    @Test
    fun `marcar una fila cambia solo esa fila y sube el anillo`() {
        val state = TodayReducer.reduce(loaded, TodayEvent.RowStateChanged(pill.slotId, CheckState.Done))

        val rows = state.content!!.sections.single().rows
        assertEquals(listOf(CheckState.Done, CheckState.Pending), rows.map { it.state })
        assertEquals(1 to 2, state.content!!.done to state.content!!.required)
    }

    @Test
    fun `una toma omitida sale del total del anillo`() {
        val state = TodayReducer.reduce(loaded, TodayEvent.RowStateChanged(walk.slotId, CheckState.Skipped))

        assertEquals(0 to 1, state.content!!.done to state.content!!.required)
    }

    @Test
    fun `un momento que no esta en pantalla no cambia nada`() {
        val state = TodayReducer.reduce(loaded, TodayEvent.RowStateChanged(slotIdOf(99), CheckState.Done))

        assertSame(loaded, state)
    }

    @Test
    fun `cerrar un deshacer antiguo no borra el vigente`() {
        val armed = TodayReducer.reduce(loaded, TodayEvent.UndoArmed(UndoToken(2, pill.slotId, day, null)))

        val state = TodayReducer.reduce(armed, TodayEvent.UndoCleared(undoId = 1))

        assertEquals(2L, state.pendingUndo?.id)
    }

    @Test
    fun `un dia nuevo descarta el deshacer de ayer`() {
        val armed = loaded.copy(pendingUndo = UndoToken(1, pill.slotId, day, null))

        val state = TodayReducer.reduce(armed, TodayEvent.ContentLoaded(content.copy(day = LocalDate(2026, 12, 2))))

        assertNull(state.pendingUndo)
    }

    @Test
    fun `otros habitos se pliega y se despliega`() {
        val open = TodayReducer.reduce(loaded, TodayEvent.OthersToggled)
        val closed = TodayReducer.reduce(open, TodayEvent.OthersToggled)

        assertTrue(open.othersExpanded)
        assertFalse(closed.othersExpanded)
    }

    private fun row(n: Int, title: String, partOfMinimum: Boolean = true) = TodayRow(
        slotId = slotIdOf(n),
        title = title,
        supportingText = null,
        doneTime = null,
        state = CheckState.Pending,
        partOfMinimum = partOfMinimum,
    )
}
