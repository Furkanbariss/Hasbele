package com.virata.hamdele.core.ads

import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest

/**
 * Reklamlar hiçbir zaman kişiselleştirilmez: dini bir uygulamanın kullanımı kişinin inancını belli
 * edebilir (KVKK md. 6), bu yüzden ilgi alanı profili için veri kullanılmasın.
 */
internal fun nonPersonalizedRequest(): AdRequest =
    AdRequest.Builder()
        .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply { putString("npa", "1") })
        .build()
