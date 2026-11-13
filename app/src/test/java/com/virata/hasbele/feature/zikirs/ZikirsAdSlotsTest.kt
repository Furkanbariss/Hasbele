package com.virata.hasbele.feature.zikirs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZikirsAdSlotsTest {

    @Test
    fun presetsAdWaitsForThePresets() {
        assertFalse(zikirsPresetsAdShown(0))
        assertTrue(zikirsPresetsAdShown(6))
    }

    @Test
    fun ownAdOnlyAfterALongList() {
        assertFalse(zikirsOwnAdShown(0))
        assertFalse(zikirsOwnAdShown(ZIKIRS_OWN_AD_MIN_ROWS - 1))
        assertTrue(zikirsOwnAdShown(ZIKIRS_OWN_AD_MIN_ROWS))
    }
}
