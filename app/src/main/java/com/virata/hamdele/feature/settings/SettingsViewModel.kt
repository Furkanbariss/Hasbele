package com.virata.hamdele.feature.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.virata.hamdele.TesbihApp
import com.virata.hamdele.core.ads.AdsController
import com.virata.hamdele.core.data.repository.SettingsRepository
import com.virata.hamdele.core.domain.model.AppSettings
import com.virata.hamdele.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    val adsController: AdsController,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
    val privacyOptionsRequired: StateFlow<Boolean> = adsController.privacyOptionsRequired

    fun onSoundChange(enabled: Boolean) =
        settingsRepository.update { it.copy(soundEnabled = enabled) }

    fun onHapticsChange(enabled: Boolean) =
        settingsRepository.update { it.copy(hapticsEnabled = enabled) }

    fun onVolumeKeysChange(enabled: Boolean) =
        settingsRepository.update { it.copy(volumeKeysEnabled = enabled) }

    fun onKeepScreenOnChange(enabled: Boolean) =
        settingsRepository.update { it.copy(keepScreenOn = enabled) }

    fun onThemeChange(theme: ThemeMode) =
        settingsRepository.update { it.copy(theme = theme) }

    fun onBeadStyleChange(id: String) =
        settingsRepository.update { it.copy(beadStyleId = id) }

    /** Reklam yalnız rıza diyaloğunda "Kabul ediyorum" ile gösterilir; rızanın anı ispat için saklanır. */
    fun onAdsConsentGiven() {
        val at = now()
        settingsRepository.update { it.withAdsConsentGiven(at) }
    }

    /** Ret ya da rızayı geri alma: reklam durur, rızanın anı silinir. */
    fun onAdsConsentDenied() = settingsRepository.update { it.withAdsConsentDenied() }

    fun onPrivacyOptions(activity: Activity) = adsController.showPrivacyOptions(activity)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as TesbihApp).container
                SettingsViewModel(
                    settingsRepository = container.settingsRepository,
                    adsController = container.adsController,
                )
            }
        }
    }
}
