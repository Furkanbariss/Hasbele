package com.virata.hasbele.core.domain.model

/**
 * Kısa kullanım ipuçları. Her sayfanın ipucu, kullanıcı o sayfaya ilk girdiğinde bir kez gösterilir;
 * hepsi baştan anlatılmaz. Görülenler [AppSettings.toursSeen] içinde saklanır.
 */
enum class Tour { COUNTER, ZIKIRS, MUEZZIN_HOME, MUEZZIN_FLOW, HISTORY }
