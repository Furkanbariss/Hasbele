package com.virata.hamdele.testing

import com.virata.hamdele.core.data.prefs.MuezzinSessionStore
import com.virata.hamdele.core.domain.muezzin.MuezzinSaved

class FakeMuezzinSessionStore(var stored: MuezzinSaved = MuezzinSaved()) : MuezzinSessionStore {
    override suspend fun read(): MuezzinSaved = stored

    override suspend fun write(saved: MuezzinSaved) {
        stored = saved
    }
}
