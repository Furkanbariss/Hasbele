package com.virata.hamdele.core.ads

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

/**
 * Bir reklamlı ekranın yerel (native) reklamlarını tutar. Her yuva (slot) yalnız ilk göründüğünde bir
 * kez istenir; kaydırıp geri gelmek yeni istek yapmaz. Ekrandan çıkınca [destroy] hepsini yok eder.
 * Geri çağrımlar ana thread'den gelir.
 */
class NativeAdPool(private val context: Context, private val adUnitId: String) {

    private val loaded = mutableStateMapOf<Int, NativeAd>()
    private val requested = mutableSetOf<Int>()
    private var destroyed = false

    /** Yüklenmiş reklamlar; Compose durumudur, reklam gelince yuva kendiliğinden çizilir. */
    val ads: Map<Int, NativeAd> get() = loaded

    fun request(slot: Int) {
        if (destroyed || !requested.add(slot)) return
        AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                // Ekran istek sürerken kapandıysa reklam gösterilmeden bırakılır.
                if (destroyed) ad.destroy() else loaded[slot] = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // Yüklenemeyen yuva boş kalır; ekranda boşluk bırakmaz.
                    Log.e(TAG, "Yerel reklam yüklenemedi (yuva $slot): ${error.code} ${error.message}")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build(),
            )
            .build()
            .loadAd(nonPersonalizedRequest())
    }

    fun destroy() {
        destroyed = true
        loaded.values.forEach { it.destroy() }
        loaded.clear()
    }

    private companion object {
        const val TAG = "NativeAdPool"
    }
}
