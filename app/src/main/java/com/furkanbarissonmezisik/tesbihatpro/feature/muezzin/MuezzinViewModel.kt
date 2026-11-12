package com.furkanbarissonmezisik.tesbihatpro.feature.muezzin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.furkanbarissonmezisik.tesbihatpro.TesbihApp
import com.furkanbarissonmezisik.tesbihatpro.core.content.MuezzinFlows
import com.furkanbarissonmezisik.tesbihatpro.core.data.repository.MuezzinRepository
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.FlowOptions
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinEngine
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinEvent
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinPreferences
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinProgress
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinSaved
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinState
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinStep
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.PrayerFlowId
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
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private var saved = MuezzinSaved()
    private var flow: MuezzinState? = null

    private val _uiState = MutableStateFlow<MuezzinUiState>(MuezzinUiState.Loading)
    val uiState: StateFlow<MuezzinUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            saved = repository.load()
            val progress = saved.progress
            // Yarıda kalan akış varsa doğrudan kalınan adıma dönülür.
            if (progress != null && !progress.finished) open(progress) else showHome()
        }
    }

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
                MuezzinViewModel(container.muezzinRepository)
            }
        }
    }
}
