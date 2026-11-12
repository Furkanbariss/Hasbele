package com.furkanbarissonmezisik.tesbihatpro.core.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    // Camide ilk dokunuş duyulmasın diye ses kapalı başlar.
    val soundEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val volumeKeysEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val beadStyleId: String = DEFAULT_BEAD_STYLE_ID,
    // Uygulama hayır için yapıldı: reklam kapalı gelir, desteklemek isteyen açık rızayla açar.
    val adsEnabled: Boolean = false,
    /** Reklam için açık rızanın verildiği an (epoch ms); KVKK'da ispat yükü bizde. Kapatınca silinir. */
    val adsConsentAt: Long? = null,
    /** İlk açılış bilgilendirmesi okundu; ayar değildir ama açılışta hazır olsun diye burada durur. */
    val welcomeSeen: Boolean = false,
) {
    companion object {
        const val DEFAULT_BEAD_STYLE_ID = "amber"
    }
}
