package com.virata.hamdele.feature.history

/** Geçmiş'te reklam sıklığı: "Bugün" kartının altında bir, sonra her 5 günde bir, en çok 3. */
internal const val HISTORY_AD_EVERY_DAYS = 5
internal const val HISTORY_MAX_ADS = 3

/** "Bugün" kartından sonraki yuva. */
internal const val HISTORY_TODAY_AD_SLOT = 0

/**
 * [index] numaralı geçmiş günün ardına konacak reklam yuvası, yoksa null. Reklam yalnız iki gün
 * arasına girer: listenin son gününden sonra reklam olmaz.
 */
internal fun historyAdSlotAfterDay(index: Int, dayCount: Int): Int? {
    val shown = index + 1
    if (shown % HISTORY_AD_EVERY_DAYS != 0 || shown >= dayCount) return null
    return (shown / HISTORY_AD_EVERY_DAYS).takeIf { it < HISTORY_MAX_ADS }
}
