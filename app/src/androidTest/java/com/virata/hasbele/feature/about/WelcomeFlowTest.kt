package com.virata.hasbele.feature.about

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.virata.hasbele.R
import com.virata.hasbele.core.ads.AdsController
import com.virata.hasbele.core.data.prefs.SettingsStore
import com.virata.hasbele.core.data.repository.SettingsRepository
import com.virata.hasbele.core.domain.model.AdsConsent
import com.virata.hasbele.core.domain.model.AppSettings
import com.virata.hasbele.core.ui.components.rememberNativeAdSlot
import com.virata.hasbele.core.ui.theme.TesbihatProTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * İlk açılışın tamamı: bilgilendirme, KVKK rıza diyaloğu, Google onay adımı ve ardından reklamlı bir
 * ekranın yuva kararı. Reklam yerine etiketli bir yazı çizilir; gerçek reklam istenmez.
 */
@RunWith(AndroidJUnit4::class)
class WelcomeFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class MemorySettingsStore : SettingsStore {
        val state = MutableStateFlow(AppSettings())
        override val settings = state
        override suspend fun update(transform: (AppSettings) -> AppSettings) {
            state.value = transform(state.value)
        }
    }

    /** SDK hazırmış gibi davranır; ilk onay adımının bitişini test elle verir. */
    private class RecordingAds : AdsController {
        override val canShowAds = MutableStateFlow(true)
        override val privacyOptionsRequired = MutableStateFlow(false)
        val formAllowed = mutableListOf<Boolean>()
        var finish: (() -> Unit)? = null

        override fun prepare(activity: Activity, allowConsentForm: Boolean, onFinished: () -> Unit) {
            formAllowed += allowConsentForm
            if (finish == null) finish = onFinished else onFinished()
        }

        override fun showPrivacyOptions(activity: Activity) = Unit
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val store = MemorySettingsStore()
    private val ads = RecordingAds()
    private val repository = SettingsRepository(store, scope)
    private val viewModel = WelcomeViewModel(repository, ads, now = { 7L })

    @After
    fun tearDown() = scope.cancel()

    private fun text(id: Int) = composeRule.activity.getString(id)

    private fun start() {
        var welcome by mutableStateOf(true)
        composeRule.setContent {
            TesbihatProTheme {
                if (welcome) {
                    WelcomeRoute(onContinue = { welcome = false }, viewModel = viewModel)
                } else {
                    // Zikirler gibi reklamlı bir ekranın yuva kararı.
                    val settings by repository.settings.collectAsState()
                    val slot = rememberNativeAdSlot(ads, settings.adsAllowed, allowConsentForm = false)
                    if (slot != null) Text("ad", Modifier.testTag("ad"))
                    Text("next screen", Modifier.testTag("next"))
                }
            }
        }
    }

    @Test
    fun continueShowsTheConsentDialogAndStaysUntilAnswered() {
        start()

        composeRule.onNodeWithText(text(R.string.welcome_continue)).performClick()

        composeRule.onNodeWithText(text(R.string.ads_consent_statement)).assertExists()
        composeRule.onNodeWithTag("next").assertDoesNotExist()
        assertEquals(AdsConsent.UNKNOWN, store.state.value.adsConsent)
    }

    @Test
    fun acceptingGrantsHandlesGoogleConsentThereAndAdsAppear() {
        start()

        composeRule.onNodeWithText(text(R.string.welcome_continue)).performClick()
        composeRule.onNodeWithText(text(R.string.ads_consent_accept)).performClick()
        composeRule.waitForIdle()

        assertEquals(AdsConsent.GRANTED, store.state.value.adsConsent)
        assertEquals(7L, store.state.value.adsConsentAt)
        assertEquals("the consent form may show on the welcome", listOf(true), ads.formAllowed)
        composeRule.onNodeWithTag("next").assertDoesNotExist()

        composeRule.runOnUiThread { ads.finish!!.invoke() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("next").assertExists()
        composeRule.onNodeWithTag("ad").assertExists()
        assertTrue(store.state.value.welcomeSeen)
    }

    @Test
    fun decliningStoresTheRefusalAndNoAdsAppear() {
        start()

        composeRule.onNodeWithText(text(R.string.welcome_continue)).performClick()
        composeRule.onNodeWithText(text(R.string.ads_consent_decline)).performClick()
        composeRule.waitForIdle()

        assertEquals(AdsConsent.DENIED, store.state.value.adsConsent)
        assertNull(store.state.value.adsConsentAt)
        assertTrue(store.state.value.welcomeSeen)
        assertEquals("ad code never runs", emptyList<Boolean>(), ads.formAllowed)
        composeRule.onNodeWithTag("next").assertExists()
        composeRule.onNodeWithTag("ad").assertDoesNotExist()
        assertFalse(store.state.value.adsAllowed)
    }
}
