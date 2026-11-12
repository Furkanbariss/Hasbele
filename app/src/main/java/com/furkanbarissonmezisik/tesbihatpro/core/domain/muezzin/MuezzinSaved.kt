package com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin

import java.time.DayOfWeek
import java.time.LocalDate

/** Kullanıcının vakit listesinde seçtiği durumlar; uygulama kapansa da korunur. */
data class MuezzinPreferences(
    val tesrik: Boolean = false,
    val bayramMorning: Boolean = false,
) {
    /** Cuma sabahı salâsı kendiliğinden eklenir, bayram sabahını kullanıcı seçer. */
    fun toFlowOptions(today: LocalDate): FlowOptions = FlowOptions(
        tesrik = tesrik,
        fridayOrBayramMorning = bayramMorning || today.dayOfWeek == DayOfWeek.FRIDAY,
    )
}

/** Yarıda bırakılan akış. Adım id'siyle saklanır; içerik değişse de doğru adım bulunur. */
data class MuezzinProgress(
    val flowId: PrayerFlowId,
    val stepId: String?,
    val finished: Boolean = false,
)

data class MuezzinSaved(
    val preferences: MuezzinPreferences = MuezzinPreferences(),
    val progress: MuezzinProgress? = null,
)
