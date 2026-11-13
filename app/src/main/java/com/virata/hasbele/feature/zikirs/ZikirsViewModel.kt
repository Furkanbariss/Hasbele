package com.virata.hasbele.feature.zikirs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.virata.hasbele.TesbihApp
import com.virata.hasbele.core.ads.AdsController
import com.virata.hasbele.core.content.PresetContent
import com.virata.hasbele.core.data.repository.SessionRepository
import com.virata.hasbele.core.data.repository.SettingsRepository
import com.virata.hasbele.core.data.repository.ZikirRepository
import com.virata.hasbele.core.domain.model.AppSettings
import com.virata.hasbele.core.domain.model.TesbihatStep
import com.virata.hasbele.core.domain.model.Zikir
import com.virata.hasbele.core.domain.model.ZikirDraft
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ZikirsUiState(
    val tesbihatSummary: String = tesbihatSummary(),
    val presets: List<Zikir> = emptyList(),
    val own: List<Zikir> = emptyList(),
)

/** Örnek: `33 · 33 · 33 · 1` */
private fun tesbihatSummary(): String =
    PresetContent.defaultTesbihat.joinToString(separator = " · ") { it.target.toString() }

class ZikirsViewModel(
    private val zikirRepository: ZikirRepository,
    private val sessionRepository: SessionRepository,
    settingsRepository: SettingsRepository,
    val adsController: AdsController,
) : ViewModel() {

    /** Reklam yuvası yalnız KVKK açık rızası varsa konur. */
    val settings: StateFlow<AppSettings> = settingsRepository.settings

    val uiState: StateFlow<ZikirsUiState> = zikirRepository.zikirs
        .map { zikirs ->
            val (presets, own) = zikirs.partition { it.isPreset }
            ZikirsUiState(
                // Tehlil tek başına sayılmaz, yalnız tesbihatın son adımıdır.
                presets = presets.filterNot { it.id == PresetContent.TEHLIL_ID },
                own = own,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ZikirsUiState())

    fun onSelectTesbihat() = sessionRepository.requestSteps(PresetContent.defaultTesbihat)

    fun onSelect(zikir: Zikir) =
        sessionRepository.requestSteps(listOf(TesbihatStep(zikir.id, zikir.defaultTarget)))

    fun onAdd(draft: ZikirDraft) {
        viewModelScope.launch { zikirRepository.add(draft) }
    }

    fun onUpdate(id: Long, draft: ZikirDraft) {
        viewModelScope.launch { zikirRepository.update(id, draft) }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch { zikirRepository.delete(id) }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as TesbihApp).container
                ZikirsViewModel(
                    zikirRepository = container.zikirRepository,
                    sessionRepository = container.sessionRepository,
                    settingsRepository = container.settingsRepository,
                    adsController = container.adsController,
                )
            }
        }
    }
}
