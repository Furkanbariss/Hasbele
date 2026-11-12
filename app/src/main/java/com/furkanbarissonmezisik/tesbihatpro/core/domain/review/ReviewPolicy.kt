package com.furkanbarissonmezisik.tesbihatpro.core.domain.review

import com.furkanbarissonmezisik.tesbihatpro.core.domain.model.DailyCount
import java.time.LocalDate

/**
 * Google Play'in değerlendirme penceresinin ne zaman deneneceği. Yalnız ibadet dışı Geçmiş
 * ekranında sorulur. Google kendi kotasıyla da sınırlar; pencere her denemede çıkmayabilir.
 */
object ReviewPolicy {
    /** Kullanıcı uygulamayı tanısın diye en az bu kadar farklı günde sayım yapılmış olmalı. */
    const val MIN_COUNTED_DAYS = 3

    /** Bir denemeden sonra en az bu kadar gün beklenir. */
    const val RETRY_DAYS = 30L

    fun shouldAsk(counts: List<DailyCount>, lastAskedOn: LocalDate?, today: LocalDate): Boolean {
        val countedDays = counts.filter { it.count > 0 }.map { it.date }.distinct().size
        if (countedDays < MIN_COUNTED_DAYS) return false
        return lastAskedOn == null || !today.isBefore(lastAskedOn.plusDays(RETRY_DAYS))
    }
}
