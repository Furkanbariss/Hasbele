package com.virata.hasbele.feature.about

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.virata.hasbele.TesbihApp
import com.virata.hasbele.core.ads.AdsController
import com.virata.hasbele.core.data.repository.SettingsRepository
import com.virata.hasbele.core.review.ReviewController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AboutViewModel(private val reviewController: ReviewController) : ViewModel() {

    fun onRateApp(activity: Activity) = reviewController.openStorePage(activity)

    fun onContact(activity: Activity, address: String) {
        try {
            activity.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:$address".toUri()))
        } catch (exception: ActivityNotFoundException) {
            Log.e(TAG, "E-posta uygulaması bulunamadı", exception)
        }
    }

    companion object {
        private const val TAG = "AboutViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as TesbihApp).container
                AboutViewModel(container.reviewController)
            }
        }
    }
}

/** İlk açılışın adımları: bilgilendirme, (rıza verildiyse) Google onayı, Tesbih ekranına geçiş. */
enum class WelcomeStep { INTRO, PREPARING_ADS, DONE }

/**
 * İlk açılış bilgilendirmesi; okunduğu kaydedilir ve bir daha kendiliğinden çıkmaz. "Anladım, başla"
 * reklam için KVKK açık rızasını bir kez sorar. Rıza verilirse Google'ın onay adımı (AB'de form) da
 * burada, kullanıcı ibadet ekranlarına geçmeden önce yapılır; reddedilirse reklam kodu hiç çalışmaz.
 */
class WelcomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val adsController: AdsController,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _step = MutableStateFlow(WelcomeStep.INTRO)
    val step: StateFlow<WelcomeStep> = _step.asStateFlow()

    fun onAdsConsentAnswered(granted: Boolean) {
        if (_step.value != WelcomeStep.INTRO) return
        if (granted) {
            val at = now()
            settingsRepository.update { it.withAdsConsentGiven(at) }
            _step.value = WelcomeStep.PREPARING_ADS
        } else {
            settingsRepository.update { it.withAdsConsentDenied().copy(welcomeSeen = true) }
            _step.value = WelcomeStep.DONE
        }
    }

    /** AB onay formu gerekiyorsa burada çıkar; bitince Tesbih ekranına geçilir. */
    fun prepareAds(activity: Activity) =
        adsController.prepare(activity, allowConsentForm = true, onFinished = ::onAdsPrepared)

    fun onAdsPrepared() {
        if (_step.value == WelcomeStep.DONE) return
        settingsRepository.update { it.copy(welcomeSeen = true) }
        _step.value = WelcomeStep.DONE
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as TesbihApp).container
                WelcomeViewModel(container.settingsRepository, container.adsController)
            }
        }
    }
}
