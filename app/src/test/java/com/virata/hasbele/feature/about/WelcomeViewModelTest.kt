package com.virata.hasbele.feature.about

import com.virata.hasbele.core.data.repository.SettingsRepository
import com.virata.hasbele.core.domain.model.AdsConsent
import com.virata.hasbele.testing.FakeAdsController
import com.virata.hasbele.testing.FakeSettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Google onayı Activity ister; onun bitişi burada [WelcomeViewModel.onAdsPrepared] ile taklit edilir. */
@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {

    private val store = FakeSettingsStore()

    private fun TestScope.viewModel(): WelcomeViewModel {
        val viewModel = WelcomeViewModel(
            SettingsRepository(store, backgroundScope),
            FakeAdsController(),
            now = { CONSENT_TIME },
        )
        runCurrent()
        return viewModel
    }

    @Test
    fun `nothing is stored before the consent question is answered`() = runTest {
        val viewModel = viewModel()

        assertEquals(WelcomeStep.INTRO, viewModel.step.value)
        assertFalse(store.current.welcomeSeen)
        assertEquals(AdsConsent.UNKNOWN, store.current.adsConsent)
    }

    @Test
    fun `accepting stores the consent and waits for google consent before leaving`() = runTest {
        val viewModel = viewModel()

        viewModel.onAdsConsentAnswered(granted = true)
        runCurrent()

        assertEquals(AdsConsent.GRANTED, store.current.adsConsent)
        assertEquals(CONSENT_TIME, store.current.adsConsentAt)
        assertEquals(WelcomeStep.PREPARING_ADS, viewModel.step.value)
        assertFalse("welcome stays until google consent is done", store.current.welcomeSeen)

        viewModel.onAdsPrepared()
        runCurrent()

        assertEquals(WelcomeStep.DONE, viewModel.step.value)
        assertTrue(store.current.welcomeSeen)
    }

    @Test
    fun `declining stores the refusal and leaves without touching ads`() = runTest {
        val viewModel = viewModel()

        viewModel.onAdsConsentAnswered(granted = false)
        runCurrent()

        assertEquals(AdsConsent.DENIED, store.current.adsConsent)
        assertNull(store.current.adsConsentAt)
        assertFalse(store.current.adsAllowed)
        assertEquals(WelcomeStep.DONE, viewModel.step.value)
        assertTrue("welcome shows only once", store.current.welcomeSeen)
    }

    @Test
    fun `a second answer is ignored`() = runTest {
        val viewModel = viewModel()
        viewModel.onAdsConsentAnswered(granted = false)
        runCurrent()

        viewModel.onAdsConsentAnswered(granted = true)
        runCurrent()

        assertEquals(AdsConsent.DENIED, store.current.adsConsent)
    }
}

private const val CONSENT_TIME = 1_000L
