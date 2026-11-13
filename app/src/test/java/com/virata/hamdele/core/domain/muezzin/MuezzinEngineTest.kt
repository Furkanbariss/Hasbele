package com.virata.hamdele.core.domain.muezzin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MuezzinEngineTest {

    private fun step(id: String, condition: StepCondition = StepCondition.ALWAYS) =
        MuezzinStep(id = id, kind = StepKind.PRAY, instruction = id, condition = condition)

    private val flow = PrayerFlow(
        id = PrayerFlowId.OGLE,
        steps = listOf(
            step("ezan"),
            step("sunnet"),
            step("kamet"),
            step("farz"),
            step("tesrik", StepCondition.TESRIK),
            step("tesbihat"),
        ),
    )

    private val steps = MuezzinEngine.visibleSteps(flow, FlowOptions())

    private fun MuezzinState.next() = MuezzinEngine.reduce(this, MuezzinEvent.Next, steps.size)
    private fun MuezzinState.back() = MuezzinEngine.reduce(this, MuezzinEvent.Back, steps.size)
    private fun MuezzinState.restart() =
        MuezzinEngine.reduce(this, MuezzinEvent.Restart, steps.size)

    private val start = MuezzinEngine.start(PrayerFlowId.OGLE, FlowOptions())

    @Test
    fun `a flow starts at its first step`() {
        assertEquals(0, start.stepIndex)
        assertFalse(start.finished)
        assertEquals(PrayerFlowId.OGLE, start.flowId)
    }

    @Test
    fun `next moves one step forward`() {
        assertEquals(1, start.next().stepIndex)
    }

    @Test
    fun `next on the last step finishes the flow`() {
        var state = start
        repeat(steps.size - 1) { state = state.next() }
        assertEquals(steps.lastIndex, state.stepIndex)
        assertFalse(state.finished)

        state = state.next()

        assertTrue(state.finished)
        assertEquals(steps.lastIndex, state.stepIndex)
    }

    @Test
    fun `next after finishing changes nothing`() {
        val finished = MuezzinState(PrayerFlowId.OGLE, stepIndex = steps.lastIndex, finished = true)

        assertEquals(finished, finished.next())
    }

    @Test
    fun `back moves one step back`() {
        assertEquals(1, start.next().next().back().stepIndex)
    }

    @Test
    fun `back on the first step stays there`() {
        assertEquals(start, start.back())
    }

    @Test
    fun `back after finishing reopens the last step`() {
        val finished = MuezzinState(PrayerFlowId.OGLE, stepIndex = steps.lastIndex, finished = true)

        val state = finished.back()

        assertFalse(state.finished)
        assertEquals(steps.lastIndex, state.stepIndex)
    }

    @Test
    fun `restart goes back to the first step`() {
        val finished = MuezzinState(PrayerFlowId.OGLE, stepIndex = steps.lastIndex, finished = true)

        val state = finished.restart()

        assertEquals(0, state.stepIndex)
        assertFalse(state.finished)
    }

    @Test
    fun `an index past the end is pulled back into the flow`() {
        val stale = MuezzinState(PrayerFlowId.OGLE, stepIndex = 40)

        assertEquals(steps.lastIndex - 1, stale.back().stepIndex)
        assertTrue(stale.next().finished)
    }

    @Test
    fun `walking forward and back over the whole flow ends where it started`() {
        var state = start
        repeat(1_000) { state = state.next() }
        assertTrue(state.finished)

        repeat(1_000) { state = state.back() }

        assertEquals(start, state)
    }

    @Test
    fun `tesrik step is hidden unless tesrik days are on`() {
        assertEquals(
            listOf("ezan", "sunnet", "kamet", "farz", "tesbihat"),
            MuezzinEngine.visibleSteps(flow, FlowOptions()).map { it.id },
        )
        assertEquals(
            listOf("ezan", "sunnet", "kamet", "farz", "tesrik", "tesbihat"),
            MuezzinEngine.visibleSteps(flow, FlowOptions(tesrik = true)).map { it.id },
        )
    }

    @Test
    fun `friday or bayram morning step shows only on those mornings`() {
        val sabah = PrayerFlow(
            PrayerFlowId.SABAH,
            listOf(step("sala", StepCondition.FRIDAY_OR_BAYRAM_MORNING), step("ezan")),
        )

        assertEquals(listOf("ezan"), MuezzinEngine.visibleSteps(sabah, FlowOptions()).map { it.id })
        assertEquals(
            listOf("sala", "ezan"),
            MuezzinEngine.visibleSteps(sabah, FlowOptions(fridayOrBayramMorning = true))
                .map { it.id },
        )
    }

    @Test
    fun `saved step id finds the same step again`() {
        assertEquals(3, MuezzinEngine.indexOf(steps, "farz"))
    }

    @Test
    fun `unknown or missing step id starts from the beginning`() {
        assertEquals(0, MuezzinEngine.indexOf(steps, "kaldirilmis-adim"))
        assertEquals(0, MuezzinEngine.indexOf(steps, null))
    }

    @Test
    fun `step hidden by a turned off condition starts from the beginning`() {
        assertEquals(0, MuezzinEngine.indexOf(steps, "tesrik"))
    }

    @Test
    fun `a flow without steps is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            PrayerFlow(PrayerFlowId.SABAH, emptyList())
        }
    }

    @Test
    fun `a flow with repeated step ids is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            PrayerFlow(PrayerFlowId.SABAH, listOf(step("ezan"), step("ezan")))
        }
    }

    @Test
    fun `a flow that shows nothing without options is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            PrayerFlow(PrayerFlowId.BAYRAM, listOf(step("tesrik", StepCondition.TESRIK)))
        }
    }
}
