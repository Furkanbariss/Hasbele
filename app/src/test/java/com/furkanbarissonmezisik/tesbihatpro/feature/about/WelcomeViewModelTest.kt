package com.furkanbarissonmezisik.tesbihatpro.feature.about

import com.furkanbarissonmezisik.tesbihatpro.core.data.repository.SettingsRepository
import com.furkanbarissonmezisik.tesbihatpro.testing.FakeSettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {

    @Test
    fun `reading the welcome is remembered so it shows only once`() = runTest {
        val store = FakeSettingsStore()
        val viewModel = WelcomeViewModel(SettingsRepository(store, backgroundScope))
        runCurrent()
        assertFalse(store.current.welcomeSeen)

        viewModel.onContinue()
        runCurrent()

        assertTrue(store.current.welcomeSeen)
    }
}
