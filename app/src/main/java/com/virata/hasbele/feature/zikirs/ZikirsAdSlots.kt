package com.virata.hasbele.feature.zikirs

/**
 * Zikirler'de en çok iki küçük reklam: biri hazır zikirlerin ardında ("Zikirlerim" başlığından önce),
 * biri kendi zikirlerin listesi uzunsa onun ardında. Reklam hiçbir satırın yerini almaz.
 */
internal const val ZIKIRS_PRESETS_AD_SLOT = 0
internal const val ZIKIRS_OWN_AD_SLOT = 1

/** Kendi zikirlerin ardına reklam konması için gereken en az satır sayısı. */
internal const val ZIKIRS_OWN_AD_MIN_ROWS = 5

/** Hazır zikirler yüklenmeden reklam konmaz; yoksa reklam tesbihat kartına yapışırdı. */
internal fun zikirsPresetsAdShown(presetCount: Int): Boolean = presetCount > 0

internal fun zikirsOwnAdShown(ownCount: Int): Boolean = ownCount >= ZIKIRS_OWN_AD_MIN_ROWS
