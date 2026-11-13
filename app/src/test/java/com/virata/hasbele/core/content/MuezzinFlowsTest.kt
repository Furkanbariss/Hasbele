package com.virata.hasbele.core.content

import com.virata.hasbele.core.domain.muezzin.FlowOptions
import com.virata.hasbele.core.domain.muezzin.MuezzinEngine
import com.virata.hasbele.core.domain.muezzin.MuezzinStep
import com.virata.hasbele.core.domain.muezzin.PrayerFlowId
import com.virata.hasbele.core.domain.muezzin.StepCondition
import com.virata.hasbele.core.domain.muezzin.StepKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Akış sıralarının Diyanet İslam İlmihali'ne ve kullanıcının kararlarına uyduğunu denetler. */
class MuezzinFlowsTest {

    private val daily = listOf(
        PrayerFlowId.SABAH,
        PrayerFlowId.OGLE,
        PrayerFlowId.IKINDI,
        PrayerFlowId.AKSAM,
        PrayerFlowId.YATSI,
    )

    private fun ids(flow: PrayerFlowId, options: FlowOptions = FlowOptions()): List<String> =
        MuezzinEngine.visibleSteps(MuezzinFlows.flow(flow), options).map { it.id }

    private fun steps(flow: PrayerFlowId): List<MuezzinStep> = MuezzinFlows.flow(flow).steps

    private fun List<String>.isBefore(first: String, second: String) =
        indexOf(first) in 0 until indexOf(second)

    @Test
    fun `every prayer has a flow`() {
        assertEquals(PrayerFlowId.entries.toSet(), MuezzinFlows.all.keys)
    }

    @Test
    fun `sabah and ikindi start tesbihat right after the farz`() {
        for (flow in listOf(PrayerFlowId.SABAH, PrayerFlowId.IKINDI)) {
            val ids = ids(flow)
            assertEquals(flow.name, listOf("farz", "ente_s_selam", "salavat"), ids.slice(
                ids.indexOf("farz")..ids.indexOf("salavat"),
            ))
            assertFalse(flow.name, "son_sunnet" in ids)
        }
    }

    @Test
    fun `ogle aksam yatsi and cuma do tesbihat after the son sunnet`() {
        for (flow in listOf(
            PrayerFlowId.OGLE, PrayerFlowId.AKSAM, PrayerFlowId.YATSI, PrayerFlowId.CUMA,
        )) {
            val ids = ids(flow)
            assertTrue(flow.name, ids.isBefore("farz", "ente_s_selam"))
            assertTrue(flow.name, ids.isBefore("ente_s_selam", "son_sunnet"))
            assertTrue(flow.name, ids.isBefore("son_sunnet", "salavat"))
        }
    }

    @Test
    fun `aksam has no first sunnet and waits before the kamet`() {
        val ids = ids(PrayerFlowId.AKSAM)

        assertFalse("ilk_sunnet" in ids)
        assertEquals(listOf("ezan", "ezan_duasi", "bekle", "kamet"), ids.take(4))
        assertEquals(StepKind.WAIT, steps(PrayerFlowId.AKSAM).first { it.id == "bekle" }.kind)
    }

    @Test
    fun `yatsi prays vitir with kunut after the son sunnet and before tesbihat`() {
        val ids = ids(PrayerFlowId.YATSI)

        assertEquals(
            listOf("son_sunnet", "vitir", "kunut", "salavat"),
            ids.slice(ids.indexOf("son_sunnet")..ids.indexOf("salavat")),
        )
    }

    @Test
    fun `rakat counts follow the ilmihal`() {
        fun instruction(flow: PrayerFlowId, id: String) =
            steps(flow).first { it.id == id }.instruction

        assertTrue(instruction(PrayerFlowId.SABAH, "ilk_sunnet").contains("2 rekât"))
        assertTrue(instruction(PrayerFlowId.SABAH, "farz").contains("2 rekât"))
        assertTrue(instruction(PrayerFlowId.OGLE, "ilk_sunnet").contains("4 rekât"))
        assertTrue(instruction(PrayerFlowId.OGLE, "farz").contains("4 rekât"))
        assertTrue(instruction(PrayerFlowId.OGLE, "son_sunnet").contains("2 rekât"))
        assertTrue(instruction(PrayerFlowId.IKINDI, "farz").contains("4 rekât"))
        assertTrue(instruction(PrayerFlowId.AKSAM, "farz").contains("3 rekât"))
        assertTrue(instruction(PrayerFlowId.AKSAM, "son_sunnet").contains("2 rekât"))
        assertTrue(instruction(PrayerFlowId.YATSI, "farz").contains("4 rekât"))
        assertTrue(instruction(PrayerFlowId.YATSI, "vitir").contains("3 rekât"))
        assertTrue(instruction(PrayerFlowId.CUMA, "ilk_sunnet").contains("4 rekât"))
        assertTrue(instruction(PrayerFlowId.CUMA, "farz").contains("2 rekât"))
        assertTrue(instruction(PrayerFlowId.CUMA, "son_sunnet").contains("4 rekât"))
    }

    @Test
    fun `sabah uses the sabah ezan and the other prayers the normal ezan`() {
        assertEquals(
            MuezzinContent.sabahEzani,
            steps(PrayerFlowId.SABAH).first { it.id == "ezan" }.recitation,
        )
        for (flow in daily - PrayerFlowId.SABAH) {
            assertEquals(flow.name, MuezzinContent.ezan, steps(flow).first { it.id == "ezan" }.recitation)
        }
    }

    @Test
    fun `cuma has sala, outer ezan, ic ezan before the hutbe and kamet after it`() {
        val ids = ids(PrayerFlowId.CUMA)

        assertEquals("cuma_salasi", ids.first())
        assertTrue(ids.isBefore("ezan", "ilk_sunnet"))
        assertTrue(ids.isBefore("ilk_sunnet", "minber_ayeti"))
        assertTrue(ids.isBefore("minber_ayeti", "ic_ezan"))
        assertTrue(ids.isBefore("ic_ezan", "hutbe"))
        assertTrue(ids.isBefore("hutbe", "kamet"))
        assertTrue(ids.isBefore("kamet", "farz"))
    }

    @Test
    fun `teravih prays twenty rakat in pairs with a sitting after every four`() {
        val ids = ids(PrayerFlowId.TERAVIH)
        val pairs = ids.filter { it.matches(Regex("teravih_\\d+")) }
        val sittings = ids.filter { it.startsWith("teravih_ara_") }

        assertEquals(10, pairs.size)
        assertEquals(5, sittings.size)
        for (sitting in 1..5) {
            assertEquals(ids.indexOf("teravih_${sitting * 2}") + 1, ids.indexOf("teravih_ara_$sitting"))
        }
    }

    @Test
    fun `teravih comes after the son sunnet, then vitir, then tesbihat`() {
        val ids = ids(PrayerFlowId.TERAVIH)

        assertTrue(ids.isBefore("farz", "son_sunnet"))
        assertTrue(ids.isBefore("son_sunnet", "teravih_1"))
        assertTrue(ids.isBefore("teravih_10", "vitir"))
        assertTrue(ids.isBefore("vitir", "kunut"))
        assertTrue(ids.isBefore("kunut", "salavat"))
        assertEquals("only the yatsi farz has a kamet", 1, ids.count { it == "kamet" })
    }

    @Test
    fun `bayram has no ezan, no kamet and no tesbihat`() {
        val flow = steps(PrayerFlowId.BAYRAM)
        val texts = flow.mapNotNull { it.recitation }

        assertFalse(MuezzinContent.ezan in texts)
        assertFalse(MuezzinContent.kamet in texts)
        assertTrue(flow.none { it.kind == StepKind.TESBIHAT })
        assertTrue(ids(PrayerFlowId.BAYRAM).isBefore("namaz", "hutbe"))
    }

    @Test
    fun `tesrik tekbiri follows the farz right away only on tesrik days`() {
        for (flow in daily + PrayerFlowId.CUMA) {
            assertFalse(flow.name, "tesrik_tekbiri" in ids(flow))

            val ids = ids(flow, FlowOptions(tesrik = true))
            assertEquals(flow.name, ids.indexOf("farz") + 1, ids.indexOf("tesrik_tekbiri"))
            assertEquals(flow.name, ids.indexOf("tesrik_tekbiri") + 1, ids.indexOf("ente_s_selam"))
        }
    }

    @Test
    fun `sabah sala shows only on friday and bayram mornings`() {
        assertFalse("sabah_salasi" in ids(PrayerFlowId.SABAH))
        assertEquals(
            "sabah_salasi",
            ids(PrayerFlowId.SABAH, FlowOptions(fridayOrBayramMorning = true)).first(),
        )
    }

    @Test
    fun `tesbihat runs in the same order everywhere and ends with the dua`() {
        val block = listOf("salavat", "tesbih_duasi", "ayetel_kursi", "tesbihat", "vehhab", "dua")
        for (flow in PrayerFlowId.entries - PrayerFlowId.BAYRAM) {
            assertEquals(flow.name, block, ids(flow).takeLast(block.size))
            assertEquals(flow.name, 1, steps(flow).count { it.kind == StepKind.TESBIHAT })
        }
    }

    @Test
    fun `every step tells the user what to do`() {
        for (flow in MuezzinFlows.all.values) {
            for (step in flow.steps) {
                assertTrue("${flow.id}/${step.id}", step.instruction.isNotBlank())
            }
        }
    }

    @Test
    fun `listening, waiting, praying and counting steps carry no text to read`() {
        for (flow in MuezzinFlows.all.values) {
            for (step in flow.steps.filter { it.kind != StepKind.RECITE }) {
                assertEquals("${flow.id}/${step.id}", null, step.recitation)
            }
        }
    }

    @Test
    fun `conditional steps are only the tesrik tekbiri and the sabah sala`() {
        val conditional = MuezzinFlows.all.values.flatMap { flow ->
            flow.steps.filter { it.condition != StepCondition.ALWAYS }.map { it.id }
        }.toSet()

        assertEquals(setOf("tesrik_tekbiri", "sabah_salasi"), conditional)
    }

    @Test
    fun `every flow can be walked to its end`() {
        for (flow in MuezzinFlows.all.values) {
            val steps = MuezzinEngine.visibleSteps(flow, FlowOptions(tesrik = true))
            assertNotNull(flow.id.name, steps.lastOrNull())
        }
    }
}
