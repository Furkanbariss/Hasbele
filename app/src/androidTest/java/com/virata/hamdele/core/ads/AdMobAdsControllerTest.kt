package com.virata.hamdele.core.ads

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdMobAdsControllerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class FakeConsent : ConsentManager {
        override var canRequestAds = false
        override var privacyOptionsRequired = false
        var gatherCalls = 0
        var lastShowForm: Boolean? = null
        var finishGather: () -> Unit = {}
        var finishPrivacyForm: () -> Unit = {}

        override fun gatherConsent(
            activity: Activity,
            showFormIfRequired: Boolean,
            onFinished: () -> Unit,
        ) {
            gatherCalls++
            lastShowForm = showFormIfRequired
            finishGather = onFinished
        }

        override fun showPrivacyOptions(activity: Activity, onFinished: () -> Unit) {
            finishPrivacyForm = onFinished
        }
    }

    private val consent = FakeConsent()
    private var sdkStarts = 0
    private var finishSdkStart: () -> Unit = {}
    private lateinit var activity: Activity
    private lateinit var controller: AdMobAdsController

    @Before
    fun setUp() {
        activity = composeRule.activity
        controller = AdMobAdsController(consent) { onReady ->
            sdkStarts++
            finishSdkStart = onReady
        }
    }

    @Test
    fun nothingRunsUntilAnAdScreenAsks() {
        assertEquals(0, consent.gatherCalls)
        assertEquals(0, sdkStarts)
        assertFalse(controller.canShowAds.value)
    }

    @Test
    fun sdkWaitsForConsent() {
        controller.prepare(activity, allowConsentForm = true)

        assertEquals(1, consent.gatherCalls)
        assertEquals(0, sdkStarts)
        assertFalse(controller.canShowAds.value)
    }

    @Test
    fun adsWaitForTheSdkAfterConsent() {
        consent.canRequestAds = true
        controller.prepare(activity, allowConsentForm = true)
        consent.finishGather()

        assertEquals(1, sdkStarts)
        assertFalse(controller.canShowAds.value)

        finishSdkStart()
        assertTrue(controller.canShowAds.value)
    }

    @Test
    fun withoutConsentTheSdkNeverStarts() {
        controller.prepare(activity, allowConsentForm = true)
        consent.finishGather()

        assertEquals(0, sdkStarts)
        assertFalse(controller.canShowAds.value)
    }

    @Test
    fun enteringASecondAdScreenDoesNotStartAgain() {
        consent.canRequestAds = true
        controller.prepare(activity, allowConsentForm = true)
        consent.finishGather()
        finishSdkStart()

        controller.prepare(activity, allowConsentForm = true)

        assertEquals(1, consent.gatherCalls)
        assertEquals(1, sdkStarts)
    }

    @Test
    fun consentIsNotAskedTwiceWhileAFormIsOpen() {
        controller.prepare(activity, allowConsentForm = true)
        controller.prepare(activity, allowConsentForm = true)

        assertEquals(1, consent.gatherCalls)
    }

    @Test
    fun failedConsentIsRetriedOnTheNextAdScreen() {
        controller.prepare(activity, allowConsentForm = true)
        consent.finishGather()

        consent.canRequestAds = true
        controller.prepare(activity, allowConsentForm = true)
        consent.finishGather()

        assertEquals(2, consent.gatherCalls)
        assertEquals(1, sdkStarts)
    }

    @Test
    fun privacyEntryFollowsTheConsentResult() {
        consent.privacyOptionsRequired = true
        controller.prepare(activity, allowConsentForm = true)
        assertFalse(controller.privacyOptionsRequired.value)

        consent.finishGather()
        assertTrue(controller.privacyOptionsRequired.value)
    }

    @Test
    fun withdrawingConsentHidesAdsAndGivingItBackShowsThem() {
        consent.canRequestAds = true
        controller.prepare(activity, allowConsentForm = true)
        consent.finishGather()
        finishSdkStart()

        controller.showPrivacyOptions(activity)
        consent.canRequestAds = false
        consent.finishPrivacyForm()
        assertFalse(controller.canShowAds.value)

        controller.showPrivacyOptions(activity)
        consent.canRequestAds = true
        consent.finishPrivacyForm()
        assertTrue(controller.canShowAds.value)
        assertEquals(1, sdkStarts)
    }

    @Test
    fun consentWithdrawnDuringSdkStartKeepsAdsHidden() {
        consent.canRequestAds = true
        controller.prepare(activity, allowConsentForm = true)
        consent.finishGather()

        consent.canRequestAds = false
        finishSdkStart()

        assertFalse(controller.canShowAds.value)
    }

    @Test
    fun worshipAdjacentScreensNeverAskForTheForm() {
        controller.prepare(activity, allowConsentForm = false)

        assertEquals(false, consent.lastShowForm)
    }

    @Test
    fun formIsAllowedWhereTheScreenAllowsIt() {
        controller.prepare(activity, allowConsentForm = true)

        assertEquals(true, consent.lastShowForm)
    }

    @Test
    fun everyCallerHearsWhenConsentIsDone() {
        var first = 0
        var second = 0
        controller.prepare(activity, allowConsentForm = true) { first++ }
        controller.prepare(activity, allowConsentForm = false) { second++ }
        assertEquals(0, first)

        consent.finishGather()

        assertEquals(1, first)
        assertEquals(1, second)
        assertEquals(1, consent.gatherCalls)
    }

    @Test
    fun afterTheSdkStartedPrepareFinishesAtOnce() {
        consent.canRequestAds = true
        controller.prepare(activity, allowConsentForm = true)
        consent.finishGather()

        var finished = 0
        controller.prepare(activity, allowConsentForm = false) { finished++ }

        assertEquals(1, finished)
        assertEquals(1, consent.gatherCalls)
    }
}
