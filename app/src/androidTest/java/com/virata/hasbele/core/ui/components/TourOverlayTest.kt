package com.virata.hasbele.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.virata.hasbele.R
import com.virata.hasbele.core.ui.theme.TesbihatProTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TourOverlayTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    private var taps = 0
    private var done = 0

    private val steps = listOf(
        TourStep("target", "👆", R.string.tour_counter_tap_title, R.string.tour_counter_tap),
        TourStep(null, "🌙", R.string.tour_counter_dim_title, R.string.tour_counter_dim),
    )

    private fun show(visible: Boolean = true) {
        composeRule.setContent {
            TesbihatProTheme {
                TourHost(steps = steps, show = visible, onDone = { done++ }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { taps++ },
                    ) {
                        Text("page", modifier = Modifier.size(80.dp).tourTarget("target"))
                    }
                }
            }
        }
    }

    @Test
    fun stepsAdvanceAndTheLastOneFinishes() {
        show()

        composeRule.onNodeWithText(text(R.string.tour_counter_tap_title)).assertExists()
        composeRule.onNodeWithText(text(R.string.tour_progress, 1, 2)).assertExists()
        composeRule.onNodeWithText(text(R.string.tour_next)).performClick()

        composeRule.onNodeWithText(text(R.string.tour_counter_dim_title)).assertExists()
        composeRule.onNodeWithText(text(R.string.tour_skip)).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.tour_done)).performClick()

        assertEquals(1, done)
    }

    @Test
    fun skipFinishesAtOnce() {
        show()

        composeRule.onNodeWithText(text(R.string.tour_skip)).performClick()

        assertEquals(1, done)
    }

    @Test
    fun touchesDoNotReachThePageWhileTheTipIsOpen() {
        show()

        composeRule.onNodeWithTag(TOUR_OVERLAY_TAG).performTouchInput {
            down(topLeft + Offset(5f, 5f))
            up()
        }

        assertEquals("a tap on the dimmed page must not count", 0, taps)
        assertEquals(0, done)
    }

    @Test
    fun nothingIsShownWhenTheTipWasSeen() {
        show(visible = false)

        composeRule.onNodeWithTag(TOUR_OVERLAY_TAG).assertDoesNotExist()
        composeRule.onNodeWithText("page").performClick()
        assertEquals(1, taps)
    }
}
