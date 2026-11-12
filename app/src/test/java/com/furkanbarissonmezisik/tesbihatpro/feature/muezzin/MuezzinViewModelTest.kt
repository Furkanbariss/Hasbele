package com.furkanbarissonmezisik.tesbihatpro.feature.muezzin

import com.furkanbarissonmezisik.tesbihatpro.core.data.repository.MuezzinRepository
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinPreferences
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinProgress
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinSaved
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.PrayerFlowId
import com.furkanbarissonmezisik.tesbihatpro.testing.FakeMuezzinSessionStore
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MuezzinViewModelTest {

    private val mainDispatcher: TestDispatcher = StandardTestDispatcher()
    private val store = FakeMuezzinSessionStore()

    /** 8 Şubat 2027 pazartesi, 12 Şubat 2027 cuma. */
    private val monday = LocalDate.of(2027, 2, 8)
    private val friday = LocalDate.of(2027, 2, 12)

    @Before
    fun setUp() = Dispatchers.setMain(mainDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(today: LocalDate = monday): MuezzinViewModel {
        val repository = MuezzinRepository(store, backgroundScope, mainDispatcher)
        val viewModel = MuezzinViewModel(repository) { today }
        runCurrent()
        return viewModel
    }

    private fun MuezzinViewModel.flow() = uiState.value as MuezzinUiState.Flow

    private fun MuezzinViewModel.home() = uiState.value as MuezzinUiState.Home

    @Test
    fun `opens on the prayer list when nothing is in progress`() = runTest(mainDispatcher) {
        val home = viewModel().home()

        assertNull(home.resume)
        assertFalse(home.isFriday)
    }

    @Test
    fun `choosing a prayer starts at its first step and saves it`() = runTest(mainDispatcher) {
        val viewModel = viewModel()

        viewModel.onSelectFlow(PrayerFlowId.OGLE)
        runCurrent()

        assertEquals(0, viewModel.flow().stepIndex)
        assertEquals("ezan", viewModel.flow().step.id)
        assertEquals(MuezzinProgress(PrayerFlowId.OGLE, "ezan"), store.stored.progress)
    }

    @Test
    fun `next and previous move through the steps and every move is saved`() =
        runTest(mainDispatcher) {
            val viewModel = viewModel()
            viewModel.onSelectFlow(PrayerFlowId.AKSAM)

            viewModel.onNext()
            viewModel.onNext()
            runCurrent()
            assertEquals("bekle", viewModel.flow().step.id)
            assertEquals("bekle", store.stored.progress?.stepId)

            viewModel.onPrevious()
            runCurrent()
            assertEquals("ezan_duasi", store.stored.progress?.stepId)
        }

    @Test
    fun `reopening goes straight back to the saved step`() = runTest(mainDispatcher) {
        store.stored = MuezzinSaved(progress = MuezzinProgress(PrayerFlowId.YATSI, "vitir"))

        val viewModel = viewModel()

        assertEquals(PrayerFlowId.YATSI, viewModel.flow().flowId)
        assertEquals("vitir", viewModel.flow().step.id)
    }

    @Test
    fun `a finished flow opens the prayer list instead`() = runTest(mainDispatcher) {
        store.stored = MuezzinSaved(
            progress = MuezzinProgress(PrayerFlowId.SABAH, "dua", finished = true),
        )

        assertNull(viewModel().home().resume)
    }

    @Test
    fun `next on the last step finishes the flow`() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        viewModel.onSelectFlow(PrayerFlowId.IKINDI)
        repeat(viewModel.flow().steps.size) { viewModel.onNext() }
        runCurrent()

        assertTrue(viewModel.flow().finished)
        assertTrue(store.stored.progress!!.finished)
    }

    @Test
    fun `closing a flow keeps it to continue later`() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        viewModel.onSelectFlow(PrayerFlowId.OGLE)
        viewModel.onNext()
        viewModel.onNext()

        viewModel.onCloseFlow()

        assertEquals(Resume(PrayerFlowId.OGLE, stepNumber = 3), viewModel.home().resume)

        viewModel.onResume()
        assertEquals("ilk_sunnet", viewModel.flow().step.id)
    }

    @Test
    fun `restart goes back to the first step`() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        viewModel.onSelectFlow(PrayerFlowId.OGLE)
        viewModel.onNext()

        viewModel.onRestart()

        assertEquals(0, viewModel.flow().stepIndex)
    }

    @Test
    fun `tesrik switch adds the tekbir and is remembered`() = runTest(mainDispatcher) {
        val viewModel = viewModel()

        viewModel.onTesrikChange(true)
        runCurrent()
        viewModel.onSelectFlow(PrayerFlowId.OGLE)

        assertTrue(viewModel.flow().steps.any { it.id == "tesrik_tekbiri" })
        assertEquals(MuezzinPreferences(tesrik = true), store.stored.preferences)
    }

    @Test
    fun `sabah sala shows on fridays by itself`() = runTest(mainDispatcher) {
        val viewModel = viewModel(today = friday)
        assertTrue(viewModel.home().isFriday)

        viewModel.onSelectFlow(PrayerFlowId.SABAH)

        assertEquals("sabah_salasi", viewModel.flow().step.id)
    }

    @Test
    fun `bayram morning switch adds the sabah sala on other days`() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        viewModel.onSelectFlow(PrayerFlowId.SABAH)
        assertEquals("ezan", viewModel.flow().step.id)
        viewModel.onCloseFlow()

        viewModel.onBayramMorningChange(true)
        viewModel.onSelectFlow(PrayerFlowId.SABAH)

        assertEquals("sabah_salasi", viewModel.flow().step.id)
    }
}
