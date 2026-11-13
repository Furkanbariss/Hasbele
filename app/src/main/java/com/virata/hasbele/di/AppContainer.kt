package com.virata.hasbele.di

import android.content.Context
import com.virata.hasbele.core.ads.AdMobAdsController
import com.virata.hasbele.core.ads.AdsController
import com.virata.hasbele.core.ads.UmpConsentManager
import com.virata.hasbele.core.data.db.AppDatabase
import com.virata.hasbele.core.data.prefs.DataStoreContentVersionStore
import com.virata.hasbele.core.data.prefs.DataStoreMuezzinSessionStore
import com.virata.hasbele.core.data.repository.MuezzinRepository
import com.virata.hasbele.core.data.prefs.DataStoreReviewStore
import com.virata.hasbele.core.data.prefs.DataStoreSessionStore
import com.virata.hasbele.core.data.prefs.DataStoreSettingsStore
import com.virata.hasbele.core.data.prefs.ReviewStore
import com.virata.hasbele.core.data.repository.HistoryRepository
import com.virata.hasbele.core.data.repository.SessionRepository
import com.virata.hasbele.core.data.repository.SettingsRepository
import com.virata.hasbele.core.data.repository.ZikirRepository
import com.virata.hasbele.core.feedback.HapticsController
import com.virata.hasbele.core.feedback.SoundController
import com.virata.hasbele.core.feedback.SoundPoolController
import com.virata.hasbele.core.feedback.VibratorHapticsController
import com.virata.hasbele.core.input.VolumeKeyDispatcher
import com.virata.hasbele.core.review.PlayReviewController
import com.virata.hasbele.core.review.ReviewController
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Tüm repository ve servislerin tek kaynağı. */
class AppContainer(context: Context) {
    /** Ekran kapansa da bitmesi gereken işler (bekleyen disk yazımları) için. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val adsController: AdsController = AdMobAdsController(
        consent = UmpConsentManager(context),
        initializeAds = { onReady ->
            applicationScope.launch(Dispatchers.IO) {
                MobileAds.initialize(context) { onReady() }
            }
        },
    )

    private val database = AppDatabase.build(context)

    val sessionRepository = SessionRepository(DataStoreSessionStore(context), applicationScope)
    val zikirRepository = ZikirRepository(
        dao = database.zikirDao(),
        contentVersion = DataStoreContentVersionStore(context),
        scope = applicationScope,
    )
    val historyRepository = HistoryRepository(database.dailyCountDao(), applicationScope)
    val settingsRepository = SettingsRepository(DataStoreSettingsStore(context), applicationScope)
    val volumeKeyDispatcher = VolumeKeyDispatcher()
    val hapticsController: HapticsController = VibratorHapticsController(context)
    val soundController: SoundController = SoundPoolController(context)
    val reviewController: ReviewController = PlayReviewController(context)
    val reviewStore: ReviewStore = DataStoreReviewStore(context)
    val muezzinRepository = MuezzinRepository(DataStoreMuezzinSessionStore(context), applicationScope)

    fun flushPendingWrites() {
        applicationScope.launch { sessionRepository.flush() }
        applicationScope.launch { historyRepository.flush() }
    }
}
