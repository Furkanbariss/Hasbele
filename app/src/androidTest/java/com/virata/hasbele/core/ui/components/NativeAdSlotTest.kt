package com.virata.hasbele.core.ui.components

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.virata.hasbele.core.ads.AdsController
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Gerçek reklam istenmesin diye verilen yuva hiç çizilmez; burada yalnız onay ve SDK hazırlığının
 * kullanıcının rızasına uyduğu ve hazır olmadan yuva verilmediği denenir.
 */
@RunWith(AndroidJUnit4::class)
class NativeAdSlotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class RecordingAdsController : AdsController {
        override val canShowAds = MutableStateFlow(false)
        override val privacyOptionsRequired = MutableStateFlow(false)
        val formAllowed = mutableListOf<Boolean>()

        override fun prepare(activity: Activity, allowConsentForm: Boolean, onFinished: () -> Unit) {
            formAllowed += allowConsentForm
            onFinished()
        }

        override fun showPrivacyOptions(activity: Activity) = Unit
    }

    private val ads = RecordingAdsController()
    private var slot: NativeAdSlot? = null

    @Test
    fun withoutConsentNothingRunsAndNoSlotIsGiven() {
        composeRule.setContent {
            slot = rememberNativeAdSlot(ads, enabled = false, allowConsentForm = true)
        }
        composeRule.waitForIdle()

        assertEquals(emptyList<Boolean>(), ads.formAllowed)
        assertNull(slot)
    }

    @Test
    fun consentPreparesOnceButGivesNoSlotUntilReady() {
        composeRule.setContent {
            slot = rememberNativeAdSlot(ads, enabled = true, allowConsentForm = true)
        }
        composeRule.waitForIdle()

        assertEquals(listOf(true), ads.formAllowed)
        assertNull(slot)
    }

    @Test
    fun consentAndReadySdkGiveASlot() {
        ads.canShowAds.value = true
        composeRule.setContent {
            slot = rememberNativeAdSlot(ads, enabled = true, allowConsentForm = false)
        }
        composeRule.waitForIdle()

        assertNotNull(slot)
    }

    @Test
    fun withdrawnConsentTakesTheSlotAway() {
        ads.canShowAds.value = true
        var enabled by mutableStateOf(true)
        composeRule.setContent {
            slot = rememberNativeAdSlot(ads, enabled = enabled, allowConsentForm = true)
        }
        composeRule.waitForIdle()
        assertNotNull(slot)

        enabled = false
        composeRule.waitForIdle()

        assertNull(slot)
    }

    @Test
    fun screensThatForbidTheFormPassThatOn() {
        composeRule.setContent {
            slot = rememberNativeAdSlot(ads, enabled = true, allowConsentForm = false)
        }
        composeRule.waitForIdle()

        assertEquals(listOf(false), ads.formAllowed)
    }

    @Test
    fun givingConsentLaterPreparesThen() {
        var enabled by mutableStateOf(false)
        composeRule.setContent {
            slot = rememberNativeAdSlot(ads, enabled = enabled, allowConsentForm = true)
        }
        composeRule.waitForIdle()
        assertEquals(0, ads.formAllowed.size)

        enabled = true
        composeRule.waitForIdle()

        assertEquals(1, ads.formAllowed.size)
    }
}
