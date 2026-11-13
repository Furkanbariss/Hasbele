package com.virata.hamdele.feature.settings

import com.virata.hamdele.core.data.repository.SettingsRepository
import com.virata.hamdele.core.domain.model.AdsConsent
import com.virata.hamdele.core.domain.model.AppSettings
import com.virata.hamdele.core.domain.model.ThemeMode
import com.virata.hamdele.testing.FakeAdsController
import com.virata.hamdele.testing.FakeSettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val store = FakeSettingsStore()

    private fun TestScope.viewModel(): SettingsViewModel {
        val viewModel = SettingsViewModel(
            SettingsRepository(store, backgroundScope),
            FakeAdsController(),
            now = { CONSENT_TIME },
        )
        runCurrent()
        return viewModel
    }

    @Test
    fun `each control changes only its own setting`() = runTest {
        val viewModel = viewModel()
        val changes: List<Pair<() -> Unit, AppSettings>> = listOf(
            { viewModel.onSoundChange(true) } to AppSettings(soundEnabled = true),
            { viewModel.onHapticsChange(false) } to AppSettings(hapticsEnabled = false),
            { viewModel.onVolumeKeysChange(false) } to AppSettings(volumeKeysEnabled = false),
            { viewModel.onKeepScreenOnChange(false) } to AppSettings(keepScreenOn = false),
            { viewModel.onThemeChange(ThemeMode.DARK) } to AppSettings(theme = ThemeMode.DARK),
            { viewModel.onBeadStyleChange("dark_wood") } to AppSettings(beadStyleId = "dark_wood"),
            { viewModel.onAdsConsentGiven() } to
                AppSettings(adsConsent = AdsConsent.GRANTED, adsConsentAt = CONSENT_TIME),
            { viewModel.onAdsConsentDenied() } to AppSettings(adsConsent = AdsConsent.DENIED),
        )

        changes.forEach { (change, expected) ->
            store.update { AppSettings() }
            runCurrent()

            change()
            runCurrent()

            assertEquals(expected, store.current)
        }
    }

    @Test
    fun `no ads until consent is given`() = runTest {
        viewModel()

        assertEquals(AdsConsent.UNKNOWN, store.current.adsConsent)
        assertFalse(store.current.adsAllowed)
        assertNull(store.current.adsConsentAt)
    }

    @Test
    fun `declining later withdraws the consent and its record`() = runTest {
        val viewModel = viewModel()
        viewModel.onAdsConsentGiven()
        runCurrent()
        assertTrue(store.current.adsAllowed)

        viewModel.onAdsConsentDenied()
        runCurrent()

        assertEquals(AdsConsent.DENIED, store.current.adsConsent)
        assertFalse(store.current.adsAllowed)
        assertNull(store.current.adsConsentAt)
    }

    @Test
    fun `consent can be given again after a refusal`() = runTest {
        val viewModel = viewModel()
        viewModel.onAdsConsentDenied()
        runCurrent()

        viewModel.onAdsConsentGiven()
        runCurrent()

        assertTrue(store.current.adsAllowed)
        assertEquals(CONSENT_TIME, store.current.adsConsentAt)
    }

    @Test
    fun `screen follows the stored settings`() = runTest {
        val viewModel = viewModel()

        store.update { it.copy(theme = ThemeMode.LIGHT) }
        runCurrent()

        assertEquals(ThemeMode.LIGHT, viewModel.settings.value.theme)
    }
}

private const val CONSENT_TIME = 1_000L
