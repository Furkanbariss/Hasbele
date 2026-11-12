package com.furkanbarissonmezisik.tesbihatpro.feature.muezzin

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.furkanbarissonmezisik.tesbihatpro.R
import com.furkanbarissonmezisik.tesbihatpro.core.content.MuezzinContent
import com.furkanbarissonmezisik.tesbihatpro.core.content.MuezzinFlows
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.FlowOptions
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinEngine
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinPreferences
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.PrayerFlowId
import com.furkanbarissonmezisik.tesbihatpro.core.ui.theme.TesbihatProTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MuezzinScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    private val selected = mutableListOf<PrayerFlowId>()
    private var resumes = 0
    private var nexts = 0
    private var previouses = 0
    private var restarts = 0
    private var closes = 0
    private var tesbihatCounts = 0

    @Test
    fun tesbihatStepOffersToCountOnTheTesbihScreen() {
        val state = flowState(PrayerFlowId.OGLE, index = 0)
        showFlow(state.copy(stepIndex = state.steps.indexOfFirst { it.id == "tesbihat" }))

        composeRule.onNodeWithText(text(R.string.muezzin_count_tesbihat)).performClick()

        assertEquals(1, tesbihatCounts)
    }

    @Test
    fun otherStepsHaveNoCountButton() {
        showFlow(flowState(PrayerFlowId.OGLE, index = 0))

        composeRule.onNodeWithText(text(R.string.muezzin_count_tesbihat)).assertDoesNotExist()
    }

    private fun showHome(state: MuezzinUiState.Home) {
        composeRule.setContent {
            TesbihatProTheme {
                MuezzinHomeScreen(
                    state = state,
                    onBack = {},
                    onSelectFlow = { selected += it },
                    onResume = { resumes++ },
                    onTesrikChange = {},
                    onBayramMorningChange = {},
                )
            }
        }
    }

    private fun flowState(flow: PrayerFlowId, index: Int, finished: Boolean = false) =
        MuezzinUiState.Flow(
            flowId = flow,
            steps = MuezzinEngine.visibleSteps(MuezzinFlows.flow(flow), FlowOptions()),
            stepIndex = index,
            finished = finished,
        )

    private fun showFlow(state: MuezzinUiState.Flow) {
        composeRule.setContent {
            TesbihatProTheme {
                MuezzinFlowScreen(
                    state = state,
                    onClose = { closes++ },
                    onNext = { nexts++ },
                    onPrevious = { previouses++ },
                    onRestart = { restarts++ },
                    onCountTesbihat = { tesbihatCounts++ },
                )
            }
        }
    }

    @Test
    fun homeListsEveryPrayerAndOpensTheChosenOne() {
        showHome(MuezzinUiState.Home(MuezzinPreferences(), isFriday = false, resume = null))

        for (id in PrayerFlowId.entries) {
            composeRule.onNode(hasScrollToNodeAction())
                .performScrollToNode(hasText(text(id.nameRes())))
        }
        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText(text(R.string.prayer_aksam)))
        composeRule.onNodeWithText(text(R.string.prayer_aksam)).performClick()

        assertEquals(listOf(PrayerFlowId.AKSAM), selected)
    }

    @Test
    fun unfinishedFlowCanBeContinued() {
        showHome(
            MuezzinUiState.Home(
                MuezzinPreferences(),
                isFriday = false,
                resume = Resume(PrayerFlowId.OGLE, stepNumber = 5),
            ),
        )

        composeRule.onNodeWithText(
            text(R.string.muezzin_resume, text(R.string.prayer_ogle), 5),
        ).performClick()

        assertEquals(1, resumes)
    }

    @Test
    fun fridayIsPointedOutOnTheCumaRow() {
        showHome(MuezzinUiState.Home(MuezzinPreferences(), isFriday = true, resume = null))

        composeRule.onNodeWithText(text(R.string.muezzin_today_friday)).assertExists()
    }

    @Test
    fun firstStepShowsWhatToDoAndTheTextToRead() {
        val state = flowState(PrayerFlowId.OGLE, index = 0)
        showFlow(state)

        composeRule.onNodeWithText(text(R.string.muezzin_step_progress, 1, state.steps.size))
            .assertExists()
        composeRule.onNodeWithText(state.step.instruction).assertExists()
        composeRule.onNodeWithText(text(R.string.muezzin_kind_recite)).assertExists()
        // Ezanda tekbir satırı üç kez geçer.
        composeRule.onAllNodesWithText(MuezzinContent.ezan.arabic!!.lines().first())
            .assertCountEquals(3)
        composeRule.onNodeWithText(text(R.string.muezzin_previous)).assertIsNotEnabled()
    }

    @Test
    fun nextAndPreviousSendTheirEvents() {
        showFlow(flowState(PrayerFlowId.OGLE, index = 2))

        composeRule.onNodeWithText(text(R.string.muezzin_previous)).assertIsEnabled().performClick()
        composeRule.onNodeWithText(text(R.string.muezzin_next)).performClick()

        assertEquals(1, previouses)
        assertEquals(1, nexts)
    }

    @Test
    fun lastStepOffersToFinish() {
        val steps = flowState(PrayerFlowId.IKINDI, index = 0).steps
        showFlow(flowState(PrayerFlowId.IKINDI, index = steps.lastIndex))

        composeRule.onNodeWithText(text(R.string.muezzin_finish)).performClick()

        assertEquals(1, nexts)
    }

    @Test
    fun finishedFlowOffersToRestartOrGoBack() {
        val steps = flowState(PrayerFlowId.SABAH, index = 0).steps
        showFlow(flowState(PrayerFlowId.SABAH, index = steps.lastIndex, finished = true))

        composeRule.onNodeWithText(text(R.string.muezzin_finished, text(R.string.prayer_sabah)))
            .assertExists()
        composeRule.onNodeWithText(text(R.string.action_restart)).performClick()
        composeRule.onNodeWithText(text(R.string.muezzin_back_to_list)).performClick()

        assertEquals(1, restarts)
        assertEquals(1, closes)
    }

    @Test
    fun meaningIsShownOnRequest() {
        val state = flowState(PrayerFlowId.OGLE, index = 0)
        showFlow(state)
        val firstMeaning = MuezzinContent.ezan.meaning!!.lines().first()

        composeRule.onNodeWithText(firstMeaning).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.muezzin_show_meaning)).performScrollTo().performClick()

        composeRule.onAllNodesWithText(firstMeaning).assertCountEquals(3)
        composeRule.onNodeWithText(text(R.string.muezzin_hide_meaning)).assertExists()
    }

    @Test
    fun copyrightedMealPointsToItsSource() {
        val state = flowState(PrayerFlowId.OGLE, index = 0)
        val index = state.steps.indexOfFirst { it.id == "ayetel_kursi" }
        showFlow(state.copy(stepIndex = index))

        composeRule.onNodeWithText(
            text(R.string.muezzin_meaning_ref, MuezzinContent.ayetelKursi.meaningRef!!),
        ).assertExists()
        composeRule.onNodeWithText(text(R.string.muezzin_show_meaning)).assertDoesNotExist()
    }
}
