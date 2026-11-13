package com.virata.hasbele.core.data.repository

import com.virata.hasbele.core.data.prefs.MuezzinSessionStore
import com.virata.hasbele.core.domain.muezzin.MuezzinSaved
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Müezzin akışının kalıcı kaydı. Kayıt açılışta bir kez okunur; her değişiklik bellekte hemen
 * görünür ve diske sırayla yazılır (yalnız en sonuncusu), böylece uygulama öldürülse de akış
 * kalınan adımdan sürer.
 */
class MuezzinRepository(
    private val store: MuezzinSessionStore,
    scope: CoroutineScope,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val writes = Channel<MuezzinSaved>(Channel.CONFLATED)

    /** Okuma bitene kadar null kalır. */
    private val saved = MutableStateFlow<MuezzinSaved?>(null)

    /** Tesbihat Tesbih ekranına devredildi ve müezzin akışına dönülmedi. */
    val tesbihatHandoff: Flow<Boolean> =
        saved.filterNotNull().map { it.tesbihatHandoff }.distinctUntilChanged()

    init {
        scope.launch(ioDispatcher) {
            // Okuma bitmeden yapılmış bir kayıt varsa diskten gelen eski değer onu ezmez.
            saved.compareAndSet(null, store.read())
            for (next in writes) store.write(next)
        }
    }

    suspend fun load(): MuezzinSaved = saved.filterNotNull().first()

    fun save(next: MuezzinSaved) {
        saved.value = next
        writes.trySend(next)
    }
}
