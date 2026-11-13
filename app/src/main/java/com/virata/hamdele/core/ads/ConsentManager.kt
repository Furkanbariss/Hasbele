package com.virata.hamdele.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.virata.hamdele.BuildConfig
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

interface ConsentManager {
    val canRequestAds: Boolean
    val privacyOptionsRequired: Boolean

    /**
     * Onay bilgisini günceller; [showFormIfRequired] ise gerekiyorsa formu gösterir. Form gösterilmezse
     * ve onay eksikse [canRequestAds] false kalır. Hata olsa da [onFinished] çağrılır.
     */
    fun gatherConsent(activity: Activity, showFormIfRequired: Boolean, onFinished: () -> Unit)

    fun showPrivacyOptions(activity: Activity, onFinished: () -> Unit)
}

class UmpConsentManager(context: Context) : ConsentManager {
    private val consentInformation: ConsentInformation by lazy {
        UserMessagingPlatform.getConsentInformation(context)
    }

    override val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    override val privacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    override fun gatherConsent(
        activity: Activity,
        showFormIfRequired: Boolean,
        onFinished: () -> Unit,
    ) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            requestParameters(activity),
            {
                if (showFormIfRequired) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        formError?.let { Log.e(TAG, "Onay formu gösterilemedi: ${it.message}") }
                        onFinished()
                    }
                } else {
                    // İbadete yakın ekranlarda form açılmaz; onay eksikse reklam gösterilmez.
                    onFinished()
                }
            },
            { requestError ->
                Log.e(TAG, "Onay bilgisi alınamadı: ${requestError.message}")
                onFinished()
            },
        )
    }

    override fun showPrivacyOptions(activity: Activity, onFinished: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            formError?.let { Log.e(TAG, "Gizlilik seçenekleri açılamadı: ${it.message}") }
            onFinished()
        }
    }

    /** AB simülasyonu yalnız debug'da ve local.properties'te cihaz kimliği varsa devreye girer. */
    private fun requestParameters(activity: Activity): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
        if (BuildConfig.UMP_TEST_DEVICE_HASH.isNotEmpty()) {
            builder.setConsentDebugSettings(
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId(BuildConfig.UMP_TEST_DEVICE_HASH)
                    .build(),
            )
        }
        return builder.build()
    }

    private companion object {
        const val TAG = "ConsentManager"
    }
}
