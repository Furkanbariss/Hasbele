package com.virata.hasbele.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {

    @Test
    fun aPageTipShowsOnlyAfterTheWelcomeAndOnlyOnce() {
        assertFalse(
            "no tip before the first-run info screen",
            AppSettings(welcomeSeen = false).shouldShowTour(Tour.COUNTER),
        )
        val fresh = AppSettings(welcomeSeen = true)
        assertTrue(fresh.shouldShowTour(Tour.COUNTER))

        val seen = fresh.withTourSeen(Tour.COUNTER)
        assertFalse(seen.shouldShowTour(Tour.COUNTER))
        assertTrue("other pages keep their own tip", seen.shouldShowTour(Tour.HISTORY))
    }

    @Test
    fun onlyExplicitConsentAllowsAds() {
        assertFalse(AppSettings(adsConsent = AdsConsent.UNKNOWN).adsAllowed)
        assertFalse(AppSettings(adsConsent = AdsConsent.DENIED).adsAllowed)
        assertTrue(AppSettings(adsConsent = AdsConsent.GRANTED).adsAllowed)
    }

    @Test
    fun givingConsentRecordsTheMomentAndDenyingErasesIt() {
        val granted = AppSettings().withAdsConsentGiven(at = 42L)
        assertEquals(AdsConsent.GRANTED, granted.adsConsent)
        assertEquals(42L, granted.adsConsentAt)

        val denied = granted.withAdsConsentDenied()
        assertEquals(AdsConsent.DENIED, denied.adsConsent)
        assertNull(denied.adsConsentAt)
    }
}
