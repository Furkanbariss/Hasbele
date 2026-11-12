package com.furkanbarissonmezisik.tesbihatpro.feature.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.furkanbarissonmezisik.tesbihatpro.TesbihApp
import com.furkanbarissonmezisik.tesbihatpro.core.ads.AdsController
import com.furkanbarissonmezisik.tesbihatpro.core.data.repository.SettingsRepository
import com.furkanbarissonmezisik.tesbihatpro.core.domain.model.AppSettings
import com.furkanbarissonmezisik.tesbihatpro.core.domain.model.ThemeMode
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

    /** Reklam yalnız rıza diyaloğunda "Kabul ediyorum" ile açılır; rızanın anı ispat için saklanır. */
    fun onAdsConsentGiven() =
        settingsRepository.update { it.copy(adsEnabled = true, adsConsentAt = now()) }

    /** Kapatmak rızayı geri almaktır: reklam durur, kayıt silinir. */
    fun onAdsTurnedOff() =
        settingsRepository.update { it.copy(adsEnabled = false, adsConsentAt = null) }

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
