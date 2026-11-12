package com.furkanbarissonmezisik.tesbihatpro.core.data.prefs

import android.content.Context
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinPreferences
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinProgress
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinSaved
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.PrayerFlowId
import java.io.IOException
import kotlinx.coroutines.flow.first

/** Müezzin akışında kalınan yer ve vakit listesindeki seçimler. */
interface MuezzinSessionStore {
    suspend fun read(): MuezzinSaved
    suspend fun write(saved: MuezzinSaved)
}

private const val TAG = "MuezzinSessionStore"

private val Context.muezzinDataStore by preferencesDataStore(
    name = "muezzin",
    corruptionHandler = ReplaceFileCorruptionHandler { exception ->
        Log.e(TAG, "Müezzin oturum dosyası bozuk, sıfırlanıyor", exception)
        emptyPreferences()
    },
)

class DataStoreMuezzinSessionStore(private val context: Context) : MuezzinSessionStore {

    override suspend fun read(): MuezzinSaved {
        val preferences = try {
            context.muezzinDataStore.data.first()
        } catch (exception: IOException) {
            Log.e(TAG, "Müezzin oturumu okunamadı", exception)
            return MuezzinSaved()
        }
        return preferences.toSaved()
    }

    override suspend fun write(saved: MuezzinSaved) {
        try {
            context.muezzinDataStore.edit { preferences ->
                preferences[Keys.TESRIK] = saved.preferences.tesrik
                preferences[Keys.BAYRAM_MORNING] = saved.preferences.bayramMorning
                val progress = saved.progress
                if (progress == null) {
                    preferences.remove(Keys.FLOW)
                    preferences.remove(Keys.STEP)
                    preferences.remove(Keys.FINISHED)
                } else {
                    preferences[Keys.FLOW] = progress.flowId.name
                    progress.stepId?.let { preferences[Keys.STEP] = it }
                        ?: preferences.remove(Keys.STEP)
                    preferences[Keys.FINISHED] = progress.finished
                }
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Müezzin oturumu yazılamadı", exception)
        }
    }

    private fun Preferences.toSaved(): MuezzinSaved {
        val flow = this[Keys.FLOW]?.let { name ->
            PrayerFlowId.entries.find { it.name == name }
                ?: run {
                    Log.e(TAG, "Bilinmeyen akış kaydı yok sayıldı: $name")
                    null
                }
        }
        return MuezzinSaved(
            preferences = MuezzinPreferences(
                tesrik = this[Keys.TESRIK] ?: false,
                bayramMorning = this[Keys.BAYRAM_MORNING] ?: false,
            ),
            progress = flow?.let {
                MuezzinProgress(
                    flowId = it,
                    stepId = this[Keys.STEP],
                    finished = this[Keys.FINISHED] ?: false,
                )
            },
        )
    }

    private object Keys {
        val TESRIK = booleanPreferencesKey("tesrik")
        val BAYRAM_MORNING = booleanPreferencesKey("bayram_morning")
        val FLOW = stringPreferencesKey("flow_id")
        val STEP = stringPreferencesKey("step_id")
        val FINISHED = booleanPreferencesKey("finished")
    }
}
