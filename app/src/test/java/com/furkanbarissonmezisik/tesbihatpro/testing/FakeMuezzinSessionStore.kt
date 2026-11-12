package com.furkanbarissonmezisik.tesbihatpro.testing

import com.furkanbarissonmezisik.tesbihatpro.core.data.prefs.MuezzinSessionStore
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinSaved

class FakeMuezzinSessionStore(var stored: MuezzinSaved = MuezzinSaved()) : MuezzinSessionStore {
    override suspend fun read(): MuezzinSaved = stored

    override suspend fun write(saved: MuezzinSaved) {
        stored = saved
    }
}
