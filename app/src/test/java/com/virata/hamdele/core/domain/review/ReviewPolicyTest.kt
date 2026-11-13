package com.virata.hamdele.core.domain.review

import com.virata.hamdele.core.domain.model.DailyCount
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPolicyTest {

    private val today = LocalDate.of(2027, 2, 8)

    private fun countedOn(vararg daysAgo: Long, count: Int = 5) =
        daysAgo.map { DailyCount(today.minusDays(it), zikirId = 1, count = count) }

    @Test
    fun `three counted days and never asked asks`() {
        assertTrue(ReviewPolicy.shouldAsk(countedOn(0, 1, 5), lastAskedOn = null, today = today))
    }

    @Test
    fun `two counted days are not enough`() {
        assertFalse(ReviewPolicy.shouldAsk(countedOn(0, 1), lastAskedOn = null, today = today))
    }

    @Test
    fun `several dhikrs on one day count as one day`() {
        val counts = listOf(
            DailyCount(today, zikirId = 1, count = 33),
            DailyCount(today, zikirId = 2, count = 33),
            DailyCount(today, zikirId = 3, count = 33),
            DailyCount(today.minusDays(1), zikirId = 1, count = 5),
        )

        assertFalse(ReviewPolicy.shouldAsk(counts, lastAskedOn = null, today = today))
    }

    @Test
    fun `days undone back to zero do not count`() {
        val counts = countedOn(0, 1) + countedOn(2, count = 0)

        assertFalse(ReviewPolicy.shouldAsk(counts, lastAskedOn = null, today = today))
    }

    @Test
    fun `not asked again within thirty days`() {
        val counts = countedOn(0, 1, 2)

        assertFalse(ReviewPolicy.shouldAsk(counts, today.minusDays(29), today))
    }

    @Test
    fun `asked again after thirty days`() {
        val counts = countedOn(0, 1, 2)

        assertTrue(ReviewPolicy.shouldAsk(counts, today.minusDays(30), today))
    }
}
