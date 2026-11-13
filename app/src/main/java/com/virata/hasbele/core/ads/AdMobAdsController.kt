package com.virata.hasbele.core.ads

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Geri çağrımlar ana thread'den gelir; durum alanları başka thread'den değişmez. */
class AdMobAdsController(
    private val consent: ConsentManager,
    private val initializeAds: (onReady: () -> Unit) -> Unit,
) : AdsController {

    private val _canShowAds = MutableStateFlow(false)
    override val canShowAds: StateFlow<Boolean> = _canShowAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)
    override val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    private var gathering = false
    private var sdkStarted = false
    private val waiting = mutableListOf<() -> Unit>()

    override fun prepare(activity: Activity, allowConsentForm: Boolean, onFinished: () -> Unit) {
        if (sdkStarted) {
            onFinished()
            return
        }
        waiting += onFinished
        // Süren bir onay adımı varsa onun sonucu beklenir; ikinci istek yapılmaz.
        if (gathering) return
        gathering = true
        consent.gatherConsent(activity, showFormIfRequired = allowConsentForm) {
            gathering = false
            onConsentChanged()
            val callbacks = waiting.toList()
            waiting.clear()
            callbacks.forEach { it() }
        }
    }

    override fun showPrivacyOptions(activity: Activity) {
        consent.showPrivacyOptions(activity, ::onConsentChanged)
    }

    private fun onConsentChanged() {
        _privacyOptionsRequired.value = consent.privacyOptionsRequired
        if (!consent.canRequestAds) {
            _canShowAds.value = false
        } else if (sdkStarted) {
            _canShowAds.value = true
        } else {
            sdkStarted = true
            initializeAds { _canShowAds.value = consent.canRequestAds }
        }
    }
}
