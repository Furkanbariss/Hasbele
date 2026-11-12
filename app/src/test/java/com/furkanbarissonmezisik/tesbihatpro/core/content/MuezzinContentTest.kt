package com.furkanbarissonmezisik.tesbihatpro.core.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MuezzinContentTest {

    private val texts = MuezzinContent.all

    @Test
    fun `every text names its diyanet source`() {
        for (text in texts) {
            assertTrue(text.id, text.source.isNotBlank())
            assertTrue(text.id, text.transliteration.isNotBlank())
        }
    }

    @Test
    fun `ids are unique`() {
        assertEquals(texts.size, texts.map { it.id }.toSet().size)
    }

    @Test
    fun `a missing meal always says where to read it`() {
        for (text in texts.filter { it.meaning == null }) {
            assertNotNull(text.id, text.meaningRef)
        }
    }

    @Test
    fun `arabic, reading and meaning lines match one to one`() {
        for (text in texts) {
            val arabic = text.arabic ?: continue
            val lines = arabic.lines().size
            assertEquals(text.id, lines, text.transliteration.lines().size)
            text.meaning?.let { assertEquals(text.id, lines, it.lines().size) }
        }
    }

    @Test
    fun `every text in a flow is listed in the content`() {
        val used = MuezzinFlows.all.values.flatMap { flow -> flow.steps.mapNotNull { it.recitation } }

        assertTrue(texts.containsAll(used))
    }

    @Test
    fun `ezan has twelve lines and kamet and sabah ezan fourteen`() {
        // Her satır müezzinin bir nefeste okuduğu bölümdür: ilk tekbirler iki satır, son tekbir tek.
        assertEquals(12, MuezzinContent.ezan.arabic!!.lines().size)
        assertEquals(14, MuezzinContent.kamet.arabic!!.lines().size)
        assertEquals(14, MuezzinContent.sabahEzani.arabic!!.lines().size)
    }

    @Test
    fun `kamet and sabah ezan add their two lines after hayye ale'l-felah`() {
        val felah = "Hayye ale'l-felâh"
        val kamet = MuezzinContent.kamet.transliteration.lines()
        val sabah = MuezzinContent.sabahEzani.transliteration.lines()

        assertEquals(listOf(felah, felah, "Kad kâmeti's-salâh", "Kad kâmeti's-salâh"), kamet.subList(8, 12))
        assertEquals(
            listOf(felah, felah, "Es-salâtü hayrün mine'n-nevm", "Es-salâtü hayrün mine'n-nevm"),
            sabah.subList(8, 12),
        )
    }
}
