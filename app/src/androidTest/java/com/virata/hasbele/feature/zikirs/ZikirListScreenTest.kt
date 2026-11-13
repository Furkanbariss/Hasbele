package com.virata.hasbele.feature.zikirs

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.virata.hasbele.R
import com.virata.hasbele.core.domain.model.Zikir
import com.virata.hasbele.core.ui.components.NativeAdSlot
import com.virata.hasbele.core.ui.theme.TesbihatProTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZikirListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun zikir(id: Long, name: String, preset: Boolean) = Zikir(
        id = id,
        arabic = null,
        transliterationTr = name,
        transliterationEn = name,
        meaningTr = null,
        meaningEn = null,
        defaultTarget = 33,
        isPreset = preset,
    )

    private val presets = listOf(zikir(1, "Preset A", true), zikir(2, "Preset B", true))
    private fun own(count: Int) = (1..count).map { zikir(100L + it, "Own $it", false) }

    private val fakeSlot: NativeAdSlot = { slot -> Text("ad $slot", Modifier.testTag("ad-$slot")) }

    private fun show(own: List<Zikir> = emptyList(), adSlot: NativeAdSlot? = null) {
        composeRule.setContent {
            TesbihatProTheme {
                ZikirListScreen(
                    uiState = ZikirsUiState(presets = presets, own = own),
                    onBack = {},
                    onSelectTesbihat = {},
                    onSelectZikir = {},
                    onAdd = {},
                    onUpdate = { _, _ -> },
                    onDelete = {},
                    adSlot = adSlot,
                )
            }
        }
    }

    private fun scrollToTag(tag: String) =
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))

    private fun scrollToText(text: String) =
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))

    @Test
    fun noAdWithoutAnAdSlot() {
        show(own = own(6))

        scrollToText("Own 6")
        composeRule.onNodeWithTag("ad-0").assertDoesNotExist()
        composeRule.onNodeWithTag("ad-1").assertDoesNotExist()
    }

    @Test
    fun adFollowsThePresetsApartFromTheRowsAround() {
        show(own = own(1), adSlot = fakeSlot)

        scrollToTag("ad-0")
        val ad = composeRule.onNodeWithTag("ad-0").getUnclippedBoundsInRoot()
        val lastPreset = composeRule.onNodeWithText("Preset B").getUnclippedBoundsInRoot()
        assertTrue("gap above the ad", ad.top - lastPreset.bottom >= 16.dp)

        val ownTitleText = context.getString(R.string.zikirs_section_own)
        scrollToText(ownTitleText)
        val ownTitle = composeRule.onNodeWithText(ownTitleText).getUnclippedBoundsInRoot()
        val adAgain = composeRule.onNodeWithTag("ad-0").getUnclippedBoundsInRoot()
        assertTrue("gap below the ad", ownTitle.top - adAgain.bottom >= 16.dp)
    }

    @Test
    fun adsNeverReplaceRowsOrTheAddButton() {
        show(own = own(1), adSlot = fakeSlot)

        composeRule.onNodeWithText(context.getString(R.string.zikirs_tesbihat_title)).assertExists()
        composeRule.onNodeWithText("Preset A").assertExists()
        composeRule.onNodeWithText("Preset B").assertExists()
        scrollToText("Own 1")
        composeRule.onNodeWithContentDescription(context.getString(R.string.zikirs_add))
            .assertExists()
    }

    @Test
    fun shortOwnListGetsNoSecondAd() {
        show(own = own(ZIKIRS_OWN_AD_MIN_ROWS - 1), adSlot = fakeSlot)

        scrollToText("Own ${ZIKIRS_OWN_AD_MIN_ROWS - 1}")
        composeRule.onNodeWithTag("ad-1").assertDoesNotExist()
    }

    @Test
    fun longOwnListGetsASecondAdAfterIt() {
        show(own = own(ZIKIRS_OWN_AD_MIN_ROWS), adSlot = fakeSlot)

        scrollToTag("ad-1")
        val ad = composeRule.onNodeWithTag("ad-1").getUnclippedBoundsInRoot()
        val lastOwn = composeRule.onNodeWithText("Own $ZIKIRS_OWN_AD_MIN_ROWS")
            .getUnclippedBoundsInRoot()
        assertTrue("second ad follows the list with a gap", ad.top - lastOwn.bottom >= 16.dp)
        composeRule.onNodeWithTag("ad-2").assertDoesNotExist()
    }
}
