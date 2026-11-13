package com.virata.hasbele.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.virata.hasbele.TesbihApp
import com.virata.hasbele.core.ads.AdsController
import com.virata.hasbele.core.data.prefs.ReviewStore
import com.virata.hasbele.core.data.repository.HistoryRepository
import com.virata.hasbele.core.data.repository.SettingsRepository
import com.virata.hasbele.core.data.repository.ZikirRepository
import com.virata.hasbele.core.domain.model.AppSettings
import com.virata.hasbele.core.domain.model.DailyCount
import com.virata.hasbele.core.domain.model.Tour
import com.virata.hasbele.core.domain.model.Zikir
import com.virata.hasbele.core.domain.review.ReviewPolicy
import com.virata.hasbele.core.review.ReviewController
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryEntry(
    /** null = zikir sonradan silinmiş */
    val zikir: Zikir?,
    val count: Int,
)

data class HistoryDay(
    val date: LocalDate,
    val total: Int,
    val entries: List<HistoryEntry>,
)

data class HistoryUiState(
    val todayTotal: Int = 0,
    val todayEntries: List<HistoryEntry> = emptyList(),
    /** Bugün hariç, yeniden eskiye. */
    val pastDays: List<HistoryDay> = emptyList(),
)

internal fun historyUiState(
    counts: List<DailyCount>,
    zikirs: List<Zikir>,
    today: LocalDate,
): HistoryUiState {
    val zikirsById = zikirs.associateBy { it.id }
    val days = counts
        .groupBy { it.date }
        .map { (date, dayCounts) ->
            HistoryDay(
                date = date,
                total = dayCounts.sumOf { it.count },
                entries = dayCounts
                    .sortedByDescending { it.count }
                    .map { HistoryEntry(zikirsById[it.zikirId], it.count) },
            )
        }
        .sortedByDescending { it.date }
    val todayDay = days.find { it.date == today }
    return HistoryUiState(
        todayTotal = todayDay?.total ?: 0,
        todayEntries = todayDay?.entries.orEmpty(),
        pastDays = days.filterNot { it.date == today },
    )
}

class HistoryViewModel(
    historyRepository: HistoryRepository,
    zikirRepository: ZikirRepository,
    val adsController: AdsController,
    private val settingsRepository: SettingsRepository,
    val reviewController: ReviewController,
    reviewStore: ReviewStore,
    today: LocalDate = LocalDate.now(),
) : ViewModel() {

    /** Reklamlar Ayarlar'dan kapatıldıysa banner gösterilmez. */
    val settings: StateFlow<AppSettings> = settingsRepository.settings

    private val _reviewDue = MutableStateFlow(false)

    /** true olunca ekran Google Play'in değerlendirme penceresini bir kez dener. */
    val reviewDue: StateFlow<Boolean> = _reviewDue.asStateFlow()

    init {
        viewModelScope.launch {
            val counts = historyRepository.observeSince(today.minusDays(DAYS_SHOWN - 1)).first()
            if (ReviewPolicy.shouldAsk(counts, reviewStore.lastAskedOn(), today)) {
                // Pencere Google kotası yüzünden çıkmasa da deneme sayılır, 30 gün beklenir.
                reviewStore.markAsked(today)
                _reviewDue.value = true
            }
        }
    }

    fun onReviewRequested() {
        _reviewDue.value = false
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.observeSince(today.minusDays(DAYS_SHOWN - 1)),
        zikirRepository.zikirs,
    ) { counts, zikirs ->
        historyUiState(counts, zikirs, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HistoryUiState())

    /** Sayfanın kullanım ipucu görüldü ya da geçildi; bir daha kendiliğinden çıkmaz. */
    fun onTourSeen(tour: Tour) = settingsRepository.update { it.withTourSeen(tour) }

    companion object {
        private const val DAYS_SHOWN = 30L
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as TesbihApp).container
                HistoryViewModel(
                    historyRepository = container.historyRepository,
                    zikirRepository = container.zikirRepository,
                    adsController = container.adsController,
                    settingsRepository = container.settingsRepository,
                    reviewController = container.reviewController,
                    reviewStore = container.reviewStore,
                )
            }
        }
    }
}
