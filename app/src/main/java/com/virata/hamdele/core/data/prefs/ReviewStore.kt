package com.virata.hamdele.core.data.prefs

import android.content.Context
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.first

/** Google Play değerlendirme penceresinin en son denendiği gün. */
interface ReviewStore {
    suspend fun lastAskedOn(): LocalDate?
    suspend fun markAsked(date: LocalDate)
}

private const val TAG = "ReviewStore"

private val Context.reviewDataStore by preferencesDataStore(
    name = "review",
    corruptionHandler = ReplaceFileCorruptionHandler { exception ->
        Log.e(TAG, "Değerlendirme dosyası bozuk, sıfırlanıyor", exception)
        emptyPreferences()
    },
)

class DataStoreReviewStore(private val context: Context) : ReviewStore {
    private val lastAskedKey = stringPreferencesKey("last_asked_on")

    override suspend fun lastAskedOn(): LocalDate? {
        val stored = try {
            context.reviewDataStore.data.first()[lastAskedKey]
        } catch (exception: IOException) {
            Log.e(TAG, "Değerlendirme tarihi okunamadı", exception)
            return null
        } ?: return null
        return try {
            LocalDate.parse(stored)
        } catch (exception: DateTimeParseException) {
            Log.e(TAG, "Değerlendirme tarihi geçersiz: $stored", exception)
            null
        }
    }

    override suspend fun markAsked(date: LocalDate) {
        try {
            context.reviewDataStore.edit { it[lastAskedKey] = date.toString() }
        } catch (exception: IOException) {
            Log.e(TAG, "Değerlendirme tarihi yazılamadı", exception)
        }
    }
}
