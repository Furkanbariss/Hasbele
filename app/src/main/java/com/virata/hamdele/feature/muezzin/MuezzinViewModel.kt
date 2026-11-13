package com.virata.hamdele.feature.muezzin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.virata.hamdele.TesbihApp
import com.virata.hamdele.core.ads.AdsController
import com.virata.hamdele.core.content.MuezzinFlows
import com.virata.hamdele.core.content.PresetContent
import com.virata.hamdele.core.data.repository.MuezzinRepository
import com.virata.hamdele.core.data.repository.SessionRepository
import com.virata.hamdele.core.data.repository.SettingsRepository
import com.virata.hamdele.core.domain.model.AppSettings
import com.virata.hamdele.core.domain.muezzin.StepKind
import com.virata.hamdele.core.input.VolumeKeyDispatcher
import com.virata.hamdele.core.domain.muezzin.FlowOptions
import com.virata.hamdele.core.domain.muezzin.MuezzinEngine
import com.virata.hamdele.core.domain.muezzin.MuezzinEvent
import com.virata.hamdele.core.domain.muezzin.MuezzinPreferences
import com.virata.hamdele.core.domain.muezzin.MuezzinProgress
import com.virata.hamdele.core.domain.muezzin.MuezzinSaved
import com.virata.hamdele.core.domain.muezzin.MuezzinState
import com.virata.hamdele.core.domain.muezzin.MuezzinStep
import com.virata.hamdele.core.domain.muezzin.PrayerFlowId
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MuezzinUiState {
    data object Loading : MuezzinUiState

    data class Home(
        val preferences: MuezzinPreferences,
        val isFriday: Boolean,
        /** Yarıda bırakılmış akış; yoksa null. */
        val resume: Resume?,
    ) : MuezzinUiState

    data class Flow(
        val flowId: PrayerFlowId,
        val steps: List<MuezzinStep>,
        val stepIndex: Int,
        val finished: Boolean,
    ) : MuezzinUiState {
        val step: MuezzinStep get() = steps[stepIndex]
    }
}

data class Resume(val flowId: PrayerFlowId, val stepNumber: Int)

class MuezzinViewModel(
    private val repository: MuezzinRepository,
    private val sessionRepository: SessionRepository,
    settingsRepository: SettingsRepository,
    private val volumeKeys: VolumeKeyDispatcher,
    /** Yalnız vakit listesinde kullanılır; adım adım akışta reklam kodu çalışmaz. */
    val adsController: AdsController,
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel(), VolumeKeyDispatcher.Listener {

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    private var saved = MuezzinSaved()
    private var flow: MuezzinState? = null

    private val _uiState = MutableStateFlow<MuezzinUiState>(MuezzinUiState.Loading)
    val uiState: StateFlow<MuezzinUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            saved = repository.load()
            val tesbihatCompleted = if (saved.tesbihatHandoff) returnFromTesbihat() else false
            val progress = saved.progress
            // Yarıda kalan akış varsa doğrudan kalınan adıma dönülür.
            if (progress != null && !progress.finished) {
                open(progress)
                // Tesbihat Tesbih ekranında bittiyse akış bir sonraki adımdan sürer.
                if (tesbihatCompleted && flowState()?.step?.kind == StepKind.TESBIHAT) onNext()
            } else {
                showHome()
            }
        }
    }

    /** Tesbih ekranındaki sayım tamamlandıysa true döner; devir kaydını kapatır. */
    private suspend fun returnFromTesbihat(): Boolean {
        val counter = sessionRepository.load()
        saved = saved.copy(tesbihatHandoff = false)
        repository.save(saved)
        return counter != null && counter.steps == PresetContent.defaultTesbihat && counter.completed
    }

    private fun flowState() = _uiState.value as? MuezzinUiState.Flow

    /**
     * Tesbihat Tesbih ekranında, her namaz için sıfırdan sayılır. Ekran geçişini çağıran yapar;
     * Tesbih ekranı devir sürdükçe "Müezzin akışına dön" düğmesini gösterir.
     */
    fun onCountTesbihat() {
        saved = saved.copy(tesbihatHandoff = true)
        repository.save(saved)
        sessionRepository.requestSteps(PresetContent.defaultTesbihat, restart = true)
    }

    /** Ses tuşları ayarı açıksa: Ses Açma = İleri, Ses Kısma = Geri. */
    override fun onVolumeUp() = onNext()

    override fun onVolumeDown() = onPrevious()

    fun attachVolumeKeys() = volumeKeys.register(this)

    fun detachVolumeKeys() = volumeKeys.unregister(this)

    fun onSelectFlow(id: PrayerFlowId) {
        flow = MuezzinEngine.start(id, options())
        saveAndShow()
    }

    fun onResume() {
        saved.progress?.let(::open)
    }

    fun onNext() = dispatch(MuezzinEvent.Next)

    fun onPrevious() = dispatch(MuezzinEvent.Back)

    fun onRestart() = dispatch(MuezzinEvent.Restart)

    /** Akıştan vakit listesine dönülür; kalınan yer saklı kalır. */
    fun onCloseFlow() {
        flow = null
        showHome()
    }

    fun onTesrikChange(enabled: Boolean) =
        updatePreferences(saved.preferences.copy(tesrik = enabled))

    fun onBayramMorningChange(enabled: Boolean) =
        updatePreferences(saved.preferences.copy(bayramMorning = enabled))

    private fun updatePreferences(preferences: MuezzinPreferences) {
        saved = saved.copy(preferences = preferences)
        repository.save(saved)
        showHome()
    }

    private fun options(): FlowOptions = saved.preferences.toFlowOptions(today())

    private fun visibleSteps(id: PrayerFlowId, options: FlowOptions): List<MuezzinStep> =
        MuezzinEngine.visibleSteps(MuezzinFlows.flow(id), options)

    private fun open(progress: MuezzinProgress) {
        val options = options()
        val steps = visibleSteps(progress.flowId, options)
        flow = MuezzinState(
            flowId = progress.flowId,
            options = options,
            stepIndex = MuezzinEngine.indexOf(steps, progress.stepId),
            finished = progress.finished,
        )
        show()
    }

    private fun dispatch(event: MuezzinEvent) {
        val current = flow ?: return
        val steps = visibleSteps(current.flowId, current.options)
        flow = MuezzinEngine.reduce(current, event, steps.size)
        saveAndShow()
    }

    private fun saveAndShow() {
        val current = flow ?: return
        val steps = visibleSteps(current.flowId, current.options)
        saved = saved.copy(
            progress = MuezzinProgress(
                flowId = current.flowId,
                stepId = steps[current.stepIndex].id,
                finished = current.finished,
            ),
        )
        repository.save(saved)
        show()
    }

    private fun show() {
        val current = flow ?: return
        _uiState.value = MuezzinUiState.Flow(
            flowId = current.flowId,
            steps = visibleSteps(current.flowId, current.options),
            stepIndex = current.stepIndex,
            finished = current.finished,
        )
    }

    private fun showHome() {
        val resume = saved.progress?.takeUnless { it.finished }?.let { progress ->
            val steps = visibleSteps(progress.flowId, options())
            Resume(progress.flowId, MuezzinEngine.indexOf(steps, progress.stepId) + 1)
        }
        _uiState.value = MuezzinUiState.Home(
            preferences = saved.preferences,
            isFriday = today().dayOfWeek == DayOfWeek.FRIDAY,
            resume = resume,
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as TesbihApp).container
                MuezzinViewModel(
                    repository = container.muezzinRepository,
                    sessionRepository = container.sessionRepository,
                    settingsRepository = container.settingsRepository,
                    volumeKeys = container.volumeKeyDispatcher,
                    adsController = container.adsController,
                )
            }
        }
    }
}
