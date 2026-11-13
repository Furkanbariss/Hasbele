package com.virata.hasbele.testing

import com.virata.hasbele.core.data.prefs.MuezzinSessionStore
import com.virata.hasbele.core.domain.muezzin.MuezzinSaved

class FakeMuezzinSessionStore(var stored: MuezzinSaved = MuezzinSaved()) : MuezzinSessionStore {
    override suspend fun read(): MuezzinSaved = stored

    override suspend fun write(saved: MuezzinSaved) {
        stored = saved
    }
}
