package com.virata.hamdele.feature.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryAdSlotsTest {

    private fun slotsFor(dayCount: Int): Map<Int, Int> =
        (0 until dayCount)
            .mapNotNull { index -> historyAdSlotAfterDay(index, dayCount)?.let { index to it } }
            .toMap()

    @Test
    fun adFollowsEveryFifthDay() {
        assertEquals(mapOf(4 to 1, 9 to 2), slotsFor(12))
    }

    @Test
    fun noAdAfterTheLastDay() {
        assertEquals(emptyMap<Int, Int>(), slotsFor(5))
        assertEquals(mapOf(4 to 1), slotsFor(10))
    }

    @Test
    fun atMostThreeAdsIncludingTheOneUnderToday() {
        val slots = slotsFor(30)

        assertEquals(mapOf(4 to 1, 9 to 2), slots)
        assertEquals(HISTORY_MAX_ADS, slots.size + 1)
    }

    @Test
    fun shortHistoryHasNoAdsBetweenDays() {
        assertNull(historyAdSlotAfterDay(0, 1))
        assertEquals(emptyMap<Int, Int>(), slotsFor(0))
        assertEquals(emptyMap<Int, Int>(), slotsFor(4))
    }
}
