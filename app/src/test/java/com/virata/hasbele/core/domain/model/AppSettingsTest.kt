package com.virata.hasbele.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {

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
