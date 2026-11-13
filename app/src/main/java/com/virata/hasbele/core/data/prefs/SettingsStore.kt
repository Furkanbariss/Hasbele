package com.virata.hasbele.core.data.prefs

import android.content.Context
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.virata.hasbele.core.domain.model.AdsConsent
import com.virata.hasbele.core.domain.model.AppSettings
import com.virata.hasbele.core.domain.model.ThemeMode
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface SettingsStore {
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}

private const val TAG = "SettingsStore"

private val Context.settingsDataStore by preferencesDataStore(
    name = "settings",
    corruptionHandler = ReplaceFileCorruptionHandler { exception ->
        Log.e(TAG, "Ayar dosyası bozuk, sıfırlanıyor", exception)
        emptyPreferences()
    },
)

class DataStoreSettingsStore(private val context: Context) : SettingsStore {

    override val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception !is IOException) throw exception
            Log.e(TAG, "Ayarlar okunamadı", exception)
            emit(emptyPreferences())
        }
        .map { it.toAppSettings() }

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        try {
            context.settingsDataStore.edit { preferences ->
                val updated = transform(preferences.toAppSettings())
                preferences[Keys.SOUND] = updated.soundEnabled
                preferences[Keys.HAPTICS] = updated.hapticsEnabled
                preferences[Keys.VOLUME_KEYS] = updated.volumeKeysEnabled
                preferences[Keys.KEEP_SCREEN_ON] = updated.keepScreenOn
                preferences[Keys.THEME] = updated.theme.name
                preferences[Keys.BEAD_STYLE] = updated.beadStyleId
                preferences[Keys.ADS_CONSENT] = updated.adsConsent.name
                // Eski sürümün açma-kapama anahtarı artık okunmaz; ilk yazımda temizlenir.
                preferences.remove(Keys.LEGACY_ADS_ENABLED)
                val consentAt = updated.adsConsentAt
                if (consentAt == null) {
                    preferences.remove(Keys.ADS_CONSENT_AT)
                } else {
                    preferences[Keys.ADS_CONSENT_AT] = consentAt
                }
                preferences[Keys.WELCOME_SEEN] = updated.welcomeSeen
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Ayarlar yazılamadı", exception)
        }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            soundEnabled = this[Keys.SOUND] ?: defaults.soundEnabled,
            hapticsEnabled = this[Keys.HAPTICS] ?: defaults.hapticsEnabled,
            volumeKeysEnabled = this[Keys.VOLUME_KEYS] ?: defaults.volumeKeysEnabled,
            keepScreenOn = this[Keys.KEEP_SCREEN_ON] ?: defaults.keepScreenOn,
            theme = ThemeMode.entries.find { it.name == this[Keys.THEME] } ?: defaults.theme,
            beadStyleId = this[Keys.BEAD_STYLE] ?: defaults.beadStyleId,
            // Bilinmeyen değer cevap verilmemiş sayılır: reklam gösterilmez.
            adsConsent = AdsConsent.entries.find { it.name == this[Keys.ADS_CONSENT] }
                ?: defaults.adsConsent,
            adsConsentAt = this[Keys.ADS_CONSENT_AT],
            welcomeSeen = this[Keys.WELCOME_SEEN] ?: defaults.welcomeSeen,
        )
    }

    private object Keys {
        val SOUND = booleanPreferencesKey("sound_enabled")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val VOLUME_KEYS = booleanPreferencesKey("volume_keys_enabled")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val THEME = stringPreferencesKey("theme")
        val BEAD_STYLE = stringPreferencesKey("bead_style_id")
        val ADS_CONSENT = stringPreferencesKey("ads_consent")
        val LEGACY_ADS_ENABLED = booleanPreferencesKey("ads_enabled")
        val ADS_CONSENT_AT = longPreferencesKey("ads_consent_at")
        val WELCOME_SEEN = booleanPreferencesKey("welcome_seen")
    }
}
