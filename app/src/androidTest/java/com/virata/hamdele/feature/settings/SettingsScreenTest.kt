package com.virata.hamdele.feature.settings

import android.view.KeyEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.virata.hamdele.R
import com.virata.hamdele.core.domain.model.AdsConsent
import com.virata.hamdele.core.domain.model.AppSettings
import com.virata.hamdele.core.domain.model.ThemeMode
import com.virata.hamdele.core.ui.components.NativeAdSlot
import com.virata.hamdele.core.ui.theme.TesbihatProTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private var privacyClicks = 0

    private fun show(
        initial: AppSettings = AppSettings(),
        privacyOptionsRequired: Boolean = false,
        adSlot: NativeAdSlot? = null,
    ): () -> AppSettings {
        var settings by mutableStateOf(initial)
        composeRule.setContent {
            TesbihatProTheme {
                SettingsScreen(
                    settings = settings,
                    privacyOptionsRequired = privacyOptionsRequired,
                    onBack = {},
                    onPrivacyOptions = { privacyClicks++ },
                    onSoundChange = { settings = settings.copy(soundEnabled = it) },
                    onHapticsChange = { settings = settings.copy(hapticsEnabled = it) },
                    onVolumeKeysChange = { settings = settings.copy(volumeKeysEnabled = it) },
                    onKeepScreenOnChange = { settings = settings.copy(keepScreenOn = it) },
                    onThemeChange = { settings = settings.copy(theme = it) },
                    onBeadStyleChange = { settings = settings.copy(beadStyleId = it) },
                    onAdsConsentGiven = { settings = settings.withAdsConsentGiven(at = 1L) },
                    onAdsConsentDenied = { settings = settings.withAdsConsentDenied() },
                    adSlot = adSlot,
                )
            }
        }
        return { settings }
    }

    private fun row(id: Int) = composeRule.onNodeWithText(context.getString(id))

    /** Liste tembel çizilir; aşağıdaki satırlar kaydırılmadan ağaçta yoktur. */
    private fun scrollTo(text: String) {
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))
    }

    private fun scrollTo(id: Int) = scrollTo(context.getString(id))

    @Test
    fun switchRowReflectsAndChangesItsSetting() {
        val settings = show()

        row(R.string.settings_sound).assertIsOff()
        row(R.string.settings_sound).performClick()

        row(R.string.settings_sound).assertIsOn()
        assertEquals(true, settings().soundEnabled)
    }

    @Test
    fun switchRowsAreIndependent() {
        val settings = show(AppSettings(soundEnabled = true))

        row(R.string.settings_vibration).performClick()

        row(R.string.settings_sound).assertIsOn()
        assertEquals(false, settings().hapticsEnabled)
    }

    @Test
    fun volumeKeysRowHasItsHint() {
        show(AppSettings(volumeKeysEnabled = false))

        row(R.string.settings_volume_keys).assertIsOff()
        row(R.string.settings_volume_keys_hint).performScrollTo()
    }

    @Test
    fun themeIsChosenFromThreeOptions() {
        val settings = show()

        row(R.string.settings_theme_system).assertIsSelected()
        row(R.string.settings_theme_dark).performClick()

        row(R.string.settings_theme_dark).assertIsSelected()
        assertEquals(ThemeMode.DARK, settings().theme)
    }

    @Test
    fun beadStyleIsChosenFromTheStyleList() {
        val settings = show()

        row(R.string.bead_style_amber).assertIsSelected()
        row(R.string.bead_style_dark_wood).performClick()

        assertEquals("dark_wood", settings().beadStyleId)
    }

    @Test
    fun unknownBeadStyleShowsTheDefaultAsSelected() {
        show(AppSettings(beadStyleId = "removed_model"))

        row(R.string.bead_style_amber).assertIsSelected()
    }

    @Test
    fun privacyEntryIsHiddenWhenNotRequired() {
        show(AppSettings(adsConsent = AdsConsent.GRANTED))

        scrollTo(R.string.settings_ads_preference)
        row(R.string.settings_privacy_options).assertDoesNotExist()
    }

    @Test
    fun privacyEntryOpensTheFormWhenRequired() {
        show(AppSettings(adsConsent = AdsConsent.GRANTED), privacyOptionsRequired = true)

        scrollTo(R.string.settings_privacy_options)
        row(R.string.settings_privacy_options).performClick()

        assertEquals(1, privacyClicks)
    }

    @Test
    fun privacyEntryIsHiddenWithoutConsent() {
        show(AppSettings(adsConsent = AdsConsent.DENIED), privacyOptionsRequired = true)

        scrollTo(R.string.settings_ads_preference)
        row(R.string.settings_privacy_options).assertDoesNotExist()
    }

    @Test
    fun adsHaveNoOnOffSwitch() {
        show()

        scrollTo(R.string.settings_ads_preference)
        composeRule.onNode(
            hasText(context.getString(R.string.settings_ads_preference)) and isToggleable(),
        ).assertDoesNotExist()
        row(R.string.settings_ads_preference).assertHasClickAction()
    }

    @Test
    fun preferenceRowShowsTheCurrentConsent() {
        val settings = show(AppSettings(adsConsent = AdsConsent.GRANTED))

        scrollTo(R.string.settings_ads_preference)
        row(R.string.settings_ads_consent_given).assertExists()
        row(R.string.settings_ads_consent_not_given).assertDoesNotExist()
        assertTrue(settings().adsAllowed)
    }

    @Test
    fun noAnswerAndRefusalBothReadAsNotGiven() {
        show(AppSettings(adsConsent = AdsConsent.UNKNOWN))

        scrollTo(R.string.settings_ads_preference)
        row(R.string.settings_ads_consent_not_given).assertExists()
    }

    @Test
    fun preferenceRowReopensTheConsentDialogAndGivesConsent() {
        val settings = show(AppSettings(adsConsent = AdsConsent.DENIED))

        scrollTo(R.string.settings_ads_preference)
        row(R.string.settings_ads_preference).performClick()

        row(R.string.ads_consent_statement).assertExists()
        assertEquals("nothing changes until answered", AdsConsent.DENIED, settings().adsConsent)

        row(R.string.ads_consent_accept).performClick()

        row(R.string.ads_consent_statement).assertDoesNotExist()
        assertEquals(AdsConsent.GRANTED, settings().adsConsent)
        row(R.string.settings_ads_consent_given).assertExists()
    }

    @Test
    fun preferenceRowWithdrawsConsent() {
        val settings = show(AppSettings(adsConsent = AdsConsent.GRANTED, adsConsentAt = 1L))

        scrollTo(R.string.settings_ads_preference)
        row(R.string.settings_ads_preference).performClick()
        row(R.string.ads_consent_decline).performClick()

        row(R.string.ads_consent_statement).assertDoesNotExist()
        assertEquals(AdsConsent.DENIED, settings().adsConsent)
        assertNull(settings().adsConsentAt)
        row(R.string.settings_ads_consent_not_given).assertExists()
    }

    @Test
    fun dismissingTheDialogChangesNothing() {
        val settings = show(AppSettings(adsConsent = AdsConsent.GRANTED, adsConsentAt = 1L))

        scrollTo(R.string.settings_ads_preference)
        row(R.string.settings_ads_preference).performClick()
        // Geri tuşu odaktaki pencereye gider; diyalog odağı alana kadar beklenir.
        composeRule.onNode(isDialog()).assertExists()
        // Yavaş emülatörde ilk geri tuşu diyalog odağı almadan gidebilir; diyalog kapanana
        // kadar yeniden denenir. Kapandıktan sonra bir daha basılmaz, yoksa etkinlik kapanır.
        repeat(5) {
            if (composeRule.onAllNodes(isDialog()).fetchSemanticsNodes().isEmpty()) return@repeat
            InstrumentationRegistry.getInstrumentation().uiAutomation.waitForIdle(500, 5_000)
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            composeRule.waitForIdle()
        }

        row(R.string.ads_consent_statement).assertDoesNotExist()
        assertEquals(AdsConsent.GRANTED, settings().adsConsent)
        assertEquals(1L, settings().adsConsentAt)
    }

    @Test
    fun noAdWithoutAnAdSlot() {
        show(AppSettings(adsConsent = AdsConsent.GRANTED))

        scrollTo(R.string.settings_ads_preference)
        composeRule.onNodeWithTag("ad").assertDoesNotExist()
    }

    @Test
    fun singleAdSitsAtTheEndApartFromTheRows() {
        show(
            AppSettings(adsConsent = AdsConsent.GRANTED),
            adSlot = { slot -> Text("ad $slot", Modifier.testTag(if (slot == 0) "ad" else "ad-$slot")) },
        )

        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag("ad"))
        val ad = composeRule.onNodeWithTag("ad").getUnclippedBoundsInRoot()
        val lastRow = row(R.string.settings_ads_hint).getUnclippedBoundsInRoot()

        assertTrue("ad is below the last row with a gap", ad.top - lastRow.bottom >= 24.dp)
        composeRule.onNodeWithTag("ad-1").assertDoesNotExist()
    }
}
