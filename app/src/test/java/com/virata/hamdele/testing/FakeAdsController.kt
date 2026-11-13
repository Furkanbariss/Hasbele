package com.virata.hamdele.testing

import android.app.Activity
import com.virata.hamdele.core.ads.AdsController
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAdsController : AdsController {
    override val canShowAds = MutableStateFlow(false)
    override val privacyOptionsRequired = MutableStateFlow(false)

    override fun prepare(activity: Activity, allowConsentForm: Boolean, onFinished: () -> Unit) =
        onFinished()

    override fun showPrivacyOptions(activity: Activity) = Unit
}
