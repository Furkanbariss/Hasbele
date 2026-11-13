package com.virata.hasbele.core.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Reklam için KVKK açık rızasının durumu. İlk açılışta bir kez sorulur; Ayarlar'daki "Reklam tercihi"
 * satırından her an verilip geri alınabilir. Yalnız [GRANTED] reklam gösterir.
 */
enum class AdsConsent { UNKNOWN, GRANTED, DENIED }

data class AppSettings(
    // Camide ilk dokunuş duyulmasın diye ses kapalı başlar.
    val soundEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val volumeKeysEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val beadStyleId: String = DEFAULT_BEAD_STYLE_ID,
    // Sessizlik rıza değildir: cevap verilmeden reklam gösterilmez.
    val adsConsent: AdsConsent = AdsConsent.UNKNOWN,
    /** Reklam için açık rızanın verildiği an (epoch ms); KVKK'da ispat yükü bizde. Geri alınca silinir. */
    val adsConsentAt: Long? = null,
    /** İlk açılış bilgilendirmesi okundu; ayar değildir ama açılışta hazır olsun diye burada durur. */
    val welcomeSeen: Boolean = false,
) {
    /** Reklam yalnız açık rızayla gösterilir; reddeden ya da cevap vermeyen reklam görmez. */
    val adsAllowed: Boolean get() = adsConsent == AdsConsent.GRANTED

    /** Rıza verildi: an kaydedilir. */
    fun withAdsConsentGiven(at: Long): AppSettings =
        copy(adsConsent = AdsConsent.GRANTED, adsConsentAt = at)

    /** Ret ya da geri alma: reklam durur, rızanın anı silinir. */
    fun withAdsConsentDenied(): AppSettings =
        copy(adsConsent = AdsConsent.DENIED, adsConsentAt = null)

    companion object {
        const val DEFAULT_BEAD_STYLE_ID = "amber"
    }
}
