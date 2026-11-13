package com.virata.hamdele.feature.history

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.virata.hamdele.R
import com.virata.hamdele.core.domain.model.Zikir
import com.virata.hamdele.core.ui.components.NativeAdSlot
import com.virata.hamdele.core.ui.theme.TesbihatProTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val zikir = Zikir(
        id = 1000,
        arabic = null,
        transliterationTr = "Ya Latif",
        transliterationEn = "Ya Latif",
        meaningTr = null,
        meaningEn = null,
        defaultTarget = 129,
        isPreset = false,
    )

    private fun show(
        state: HistoryUiState,
        onBack: () -> Unit = {},
        adSlot: NativeAdSlot? = null,
    ) {
        composeRule.setContent {
            TesbihatProTheme {
                HistoryScreen(uiState = state, onBack = onBack, adSlot = adSlot)
            }
        }
    }

    private fun longDate(date: LocalDate): String =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(context.resources.configuration.locales[0])
            .format(date)

    @Test
    fun emptyDayExplainsThatNothingWasCounted() {
        show(HistoryUiState())

        composeRule.onNodeWithText(context.getString(R.string.history_empty)).assertIsDisplayed()
        composeRule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun todayShowsTheTotalAndEachZikir() {
        show(
            HistoryUiState(
                todayTotal = 142,
                todayEntries = listOf(HistoryEntry(zikir, 129), HistoryEntry(null, 13)),
            ),
        )

        composeRule.onNodeWithText("142").assertIsDisplayed()
        composeRule.onNodeWithText("Ya Latif").assertIsDisplayed()
        composeRule.onNodeWithText("129").assertIsDisplayed()
    }

    @Test
    fun deletedZikirIsNamedAsDeleted() {
        show(HistoryUiState(todayTotal = 13, todayEntries = listOf(HistoryEntry(null, 13))))

        composeRule.onNodeWithText(context.getString(R.string.history_deleted_zikir))
            .assertIsDisplayed()
    }

    @Test
    fun pastDaysShowTheirDateAndTotal() {
        val date = LocalDate.of(2027, 2, 7)
        show(
            HistoryUiState(
                pastDays = listOf(
                    HistoryDay(date, total = 261, entries = listOf(HistoryEntry(zikir, 261))),
                ),
            ),
        )

        composeRule.onNodeWithText(context.getString(R.string.history_last_30_days))
            .assertIsDisplayed()
        composeRule.onNodeWithText(longDate(date)).assertIsDisplayed()
    }

    @Test
    fun pastSectionIsHiddenWithoutPastDays() {
        show(HistoryUiState())

        composeRule.onNodeWithText(context.getString(R.string.history_last_30_days))
            .assertDoesNotExist()
    }

    @Test
    fun backButtonLeavesTheScreen() {
        var wentBack = false
        show(HistoryUiState(), onBack = { wentBack = true })

        composeRule.onNodeWithContentDescription(context.getString(R.string.action_back))
            .performClick()

        assertTrue(wentBack)
    }

    private val fakeAdSlot: NativeAdSlot = { slot ->
        Text(text = "ad $slot", modifier = Modifier.testTag("ad-$slot"))
    }

    private fun pastDays(count: Int): List<HistoryDay> =
        (1..count).map { offset ->
            val date = LocalDate.of(2027, 2, 8).minusDays(offset.toLong())
            HistoryDay(date, total = offset, entries = listOf(HistoryEntry(zikir, offset)))
        }

    private fun scrollTo(tag: String) {
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
    }

    private fun scrollToDay(day: HistoryDay) {
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(longDate(day.date)))
    }

    /** [tag] reklamı, [above] gününün altında ve [below] gününün üstünde durur. */
    private fun assertAdBetween(tag: String, above: HistoryDay, below: HistoryDay) {
        val ad = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
        val upper = composeRule.onNodeWithText(longDate(above.date)).getUnclippedBoundsInRoot()
        val lower = composeRule.onNodeWithText(longDate(below.date)).getUnclippedBoundsInRoot()
        assertTrue("$tag is below ${above.date}", ad.top >= upper.bottom)
        assertTrue("$tag is above ${below.date}", ad.bottom <= lower.top)
    }

    @Test
    fun noAdSlotsWithoutAnAdSlot() {
        show(HistoryUiState(pastDays = pastDays(12)))

        composeRule.onNodeWithTag("ad-0").assertDoesNotExist()
    }

    @Test
    fun adSlotsSitBetweenTodayAndEveryFifthDay() {
        val days = pastDays(12)
        show(HistoryUiState(pastDays = days), adSlot = fakeAdSlot)

        val today = composeRule.onNodeWithText(context.getString(R.string.history_today))
            .getUnclippedBoundsInRoot()
        val firstAd = composeRule.onNodeWithTag("ad-0").getUnclippedBoundsInRoot()
        val title = composeRule.onNodeWithText(context.getString(R.string.history_last_30_days))
            .getUnclippedBoundsInRoot()
        assertTrue("first ad is under today", firstAd.top >= today.bottom)
        assertTrue("first ad is above the past days", firstAd.bottom <= title.top)

        scrollTo("ad-1")
        assertAdBetween("ad-1", above = days[4], below = days[5])

        scrollTo("ad-2")
        assertAdBetween("ad-2", above = days[9], below = days[10])

        scrollToDay(days.last())
        composeRule.onNodeWithTag("ad-3").assertDoesNotExist()
    }

    @Test
    fun noAdAfterTheLastDay() {
        val days = pastDays(5)
        show(HistoryUiState(pastDays = days), adSlot = fakeAdSlot)

        composeRule.onNodeWithTag("ad-0").assertExists()
        scrollToDay(days.last())
        composeRule.onNodeWithTag("ad-1").assertDoesNotExist()
    }
}
