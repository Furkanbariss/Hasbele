package com.furkanbarissonmezisik.tesbihatpro.core.data.repository

import com.furkanbarissonmezisik.tesbihatpro.core.data.prefs.MuezzinSessionStore
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinSaved
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Müezzin akışının kalıcı kaydı. Her adım değişimi hemen yazılır; yazımlar sıraya girer ve yalnız en
 * sonuncusu yazılır, böylece uygulama öldürülse de akış kalınan adımdan sürer.
 */
class MuezzinRepository(
    private val store: MuezzinSessionStore,
    scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val writes = Channel<MuezzinSaved>(Channel.CONFLATED)

    init {
        scope.launch(ioDispatcher) {
            for (saved in writes) store.write(saved)
        }
    }

    suspend fun load(): MuezzinSaved = withContext(ioDispatcher) { store.read() }

    fun save(saved: MuezzinSaved) {
        writes.trySend(saved)
    }
}
