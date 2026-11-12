package com.furkanbarissonmezisik.tesbihatpro.feature.about

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.furkanbarissonmezisik.tesbihatpro.R
import com.furkanbarissonmezisik.tesbihatpro.core.ui.theme.TesbihatProTheme
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
    private var continues = 0

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

    @Test
    fun welcomeExplainsTheAppAndContinues() {
        composeRule.setContent {
            TesbihatProTheme { WelcomeScreen(onContinue = { continues++ }) }
        }

        composeRule.onNodeWithText(text(R.string.welcome_source)).performScrollTo()
        composeRule.onNodeWithText(text(R.string.welcome_continue)).performClick()

        assertEquals(1, continues)
    }
}
