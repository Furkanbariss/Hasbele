package com.virata.hasbele.core.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface AdsController {
    /** Onay alınıp SDK hazır olana kadar false kalır; false iken reklam isteği yapılmaz. */
    val canShowAds: StateFlow<Boolean>

    /** Ayarlarda "Gizlilik seçenekleri" girişinin gösterilip gösterilmeyeceği. */
    val privacyOptionsRequired: StateFlow<Boolean>

    /**
     * Google'ın onay bilgisini (UMP) günceller ve izin varsa SDK'yı başlatır. Yalnız kullanıcı KVKK
     * açık rızası verdiyse çağrılır.
     *
     * [allowConsentForm] AB onay formunun bu ekranda açılıp açılamayacağıdır. Form ilk açılışta rıza
     * verilince ya da Geçmiş/Ayarlar'da açılabilir; Zikirler ve Müezzinlik vakit listesinde açılmaz,
     * orada form gerekiyorsa reklam gösterilmez. [onFinished] onay adımı bitince (SDK'nın hazır olmasını
     * beklemeden) ana thread'den bir kez çağrılır.
     */
    fun prepare(activity: Activity, allowConsentForm: Boolean, onFinished: () -> Unit = {})

    fun showPrivacyOptions(activity: Activity)
}
