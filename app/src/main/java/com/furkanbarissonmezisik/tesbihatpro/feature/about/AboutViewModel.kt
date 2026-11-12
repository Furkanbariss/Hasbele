package com.furkanbarissonmezisik.tesbihatpro.feature.about

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
import com.furkanbarissonmezisik.tesbihatpro.TesbihApp
import com.furkanbarissonmezisik.tesbihatpro.core.data.repository.SettingsRepository
import com.furkanbarissonmezisik.tesbihatpro.core.review.ReviewController

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

/** İlk açılış bilgilendirmesi; okunduğu kaydedilir ve bir daha kendiliğinden çıkmaz. */
class WelcomeViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    fun onContinue() = settingsRepository.update { it.copy(welcomeSeen = true) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as TesbihApp).container
                WelcomeViewModel(container.settingsRepository)
            }
        }
    }
}
