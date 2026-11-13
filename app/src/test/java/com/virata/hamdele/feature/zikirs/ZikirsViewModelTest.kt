package com.virata.hamdele.feature.zikirs

import com.virata.hamdele.core.content.PresetContent
import com.virata.hamdele.core.data.repository.SessionRepository
import com.virata.hamdele.core.data.repository.SettingsRepository
import com.virata.hamdele.core.data.repository.ZikirRepository
import com.virata.hamdele.core.domain.model.AdsConsent
import com.virata.hamdele.core.domain.model.TesbihatStep
import com.virata.hamdele.core.domain.model.ZikirDraft
import com.virata.hamdele.testing.FakeAdsController
import com.virata.hamdele.testing.FakeContentVersionStore
import com.virata.hamdele.testing.FakeSessionStore
import com.virata.hamdele.testing.FakeSettingsStore
import com.virata.hamdele.testing.FakeZikirDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ZikirsViewModelTest {

    private val mainDispatcher: TestDispatcher = StandardTestDispatcher()
    private val draft = ZikirDraft(name = "Ya Latif", arabic = null, meaning = null, target = 129)
    private lateinit var sessionRepository: SessionRepository
    private val settingsStore = FakeSettingsStore()

    @Before
    fun setUp() = Dispatchers.setMain(mainDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.createViewModel(): ZikirsViewModel {
        sessionRepository = SessionRepository(FakeSessionStore(), backgroundScope, mainDispatcher)
        val viewModel = ZikirsViewModel(
            ZikirRepository(FakeZikirDao(), FakeContentVersionStore(), backgroundScope),
            sessionRepository,
            SettingsRepository(settingsStore, backgroundScope),
            FakeAdsController(),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
        return viewModel
    }

    @Test
    fun `tesbihat summary shows every step target`() = runTest(mainDispatcher) {
        assertEquals("33 · 33 · 33 · 1", createViewModel().uiState.value.tesbihatSummary)
    }

    @Test
    fun `presets are listed without the closing tehlil`() = runTest(mainDispatcher) {
        val presets = createViewModel().uiState.value.presets

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L), presets.map { it.id })
    }

    @Test
    fun `new zikir appears under the users own`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()

        viewModel.onAdd(draft)
        runCurrent()

        assertEquals(listOf("Ya Latif"), viewModel.uiState.value.own.map { it.transliterationTr })
        assertFalse(viewModel.uiState.value.presets.any { it.transliterationTr == "Ya Latif" })
    }

    @Test
    fun `selecting tesbihat requests the default steps`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()

        viewModel.onSelectTesbihat()

        assertEquals(PresetContent.defaultTesbihat, sessionRepository.requestedSteps.value)
    }

    @Test
    fun `selecting a zikir requests one step with its target`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()

        viewModel.onSelect(viewModel.uiState.value.presets.last())

        assertEquals(
            listOf(TesbihatStep(PresetContent.SALAVAT_ID, target = 100)),
            sessionRepository.requestedSteps.value,
        )
    }

    @Test
    fun `update and delete change the users list`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        viewModel.onAdd(draft)
        runCurrent()
        val id = viewModel.uiState.value.own.single().id

        viewModel.onUpdate(id, draft.copy(name = "Ya Hafiz"))
        runCurrent()
        assertEquals("Ya Hafiz", viewModel.uiState.value.own.single().transliterationTr)

        viewModel.onDelete(id)
        runCurrent()
        assertTrue(viewModel.uiState.value.own.isEmpty())
    }

    @Test
    fun `ads appear on the zikir list only after consent`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        assertFalse(viewModel.settings.value.adsAllowed)

        settingsStore.update { it.copy(adsConsent = AdsConsent.GRANTED) }
        runCurrent()
        assertTrue(viewModel.settings.value.adsAllowed)

        settingsStore.update { it.copy(adsConsent = AdsConsent.DENIED) }
        runCurrent()
        assertFalse(viewModel.settings.value.adsAllowed)
    }
}
