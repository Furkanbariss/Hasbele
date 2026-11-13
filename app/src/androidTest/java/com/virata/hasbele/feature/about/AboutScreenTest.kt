package com.virata.hasbele.feature.about

import android.view.KeyEvent
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.virata.hasbele.R
import com.virata.hasbele.core.ui.theme.TesbihatProTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    private var rates = 0
    private var contacts = 0

    private fun showAbout() {
        composeRule.setContent {
            TesbihatProTheme {
                AboutScreen(
                    versionName = "2.0",
                    onBack = {},
                    onRateApp = { rates++ },
                    onContact = { contacts++ },
                )
            }
        }
    }

    @Test
    fun aboutSaysItIsNotAnOfficialDiyanetApp() {
        showAbout()

        composeRule.onNodeWithText(text(R.string.about_religious)).performScrollTo()
    }

    @Test
    fun privacyPolicyCanBeReadInsideTheApp() {
        showAbout()

        composeRule.onNodeWithText(text(R.string.settings_privacy_policy)).performScrollTo()
            .performClick()
        composeRule.onNodeWithText(text(R.string.privacy_policy_text)).assertExists()

        composeRule.onNodeWithText(text(R.string.action_close)).performClick()
        composeRule.onNodeWithText(text(R.string.privacy_policy_text)).assertDoesNotExist()
    }

    @Test
    fun licensesAreListed() {
        showAbout()

        composeRule.onNodeWithText(text(R.string.about_licenses)).performScrollTo().performClick()

        composeRule.onNodeWithText(text(R.string.about_licenses_text)).assertExists()
    }

    @Test
    fun rateAndContactRowsSendTheirEvents() {
        showAbout()

        composeRule.onNodeWithText(text(R.string.settings_rate_app)).performScrollTo().performClick()
        composeRule.onNodeWithText(text(R.string.about_contact)).performScrollTo().performClick()

        assertEquals(1, rates)
        assertEquals(1, contacts)
    }

    @Test
    fun versionIsShown() {
        showAbout()

        composeRule.onNodeWithText(text(R.string.settings_about_version, "2.0")).performScrollTo()
    }

    private val answers = mutableListOf<Boolean>()

    private fun showWelcome(busy: Boolean = false) {
        composeRule.setContent {
            TesbihatProTheme {
                WelcomeScreen(busy = busy, onAdsConsentAnswered = { answers += it })
            }
        }
    }

    @Test
    fun welcomeExplainsTheAppAndItsAds() {
        showWelcome()

        composeRule.onNodeWithText(text(R.string.welcome_source)).performScrollTo()
        composeRule.onNodeWithText(text(R.string.welcome_privacy)).performScrollTo()
    }

    @Test
    fun continueAsksForAdConsentBeforeLeaving() {
        showWelcome()

        composeRule.onNodeWithText(text(R.string.welcome_continue)).performClick()

        composeRule.onNodeWithText(text(R.string.ads_consent_statement)).assertExists()
        assertEquals(emptyList<Boolean>(), answers)
    }

    @Test
    fun acceptingAnswersYes() {
        showWelcome()

        composeRule.onNodeWithText(text(R.string.welcome_continue)).performClick()
        composeRule.onNodeWithText(text(R.string.ads_consent_accept)).performClick()

        composeRule.onNodeWithText(text(R.string.ads_consent_statement)).assertDoesNotExist()
        assertEquals(listOf(true), answers)
    }

    @Test
    fun decliningAnswersNo() {
        showWelcome()

        composeRule.onNodeWithText(text(R.string.welcome_continue)).performClick()
        composeRule.onNodeWithText(text(R.string.ads_consent_decline)).performClick()

        composeRule.onNodeWithText(text(R.string.ads_consent_statement)).assertDoesNotExist()
        assertEquals(listOf(false), answers)
    }

    @Test
    fun dismissingTheDialogIsNoAnswerAndStaysOnTheWelcome() {
        showWelcome()

        composeRule.onNodeWithText(text(R.string.welcome_continue)).performClick()
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

        composeRule.onNodeWithText(text(R.string.ads_consent_statement)).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.welcome_continue)).assertIsEnabled()
        assertEquals(emptyList<Boolean>(), answers)
    }

    @Test
    fun whileGoogleConsentRunsTheButtonWaits() {
        showWelcome(busy = true)

        composeRule.onNode(hasClickAction()).assertIsNotEnabled()
        composeRule.onNodeWithText(text(R.string.welcome_continue)).assertDoesNotExist()
    }
}
