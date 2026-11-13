package com.virata.hasbele.core.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Google Play değerlendirmesi. Yalnız ibadet dışı ekranlardan (Geçmiş, Ayarlar) çağrılır; Tesbih ve
 * Zikirler'de pencere açılmaz.
 */
interface ReviewController {
    /**
     * Google Play'in uygulama içi değerlendirme penceresini dener. Google kotası doluysa ya da
     * uygulama Play'den kurulmadıysa pencere çıkmaz; bu bir hata değildir ve uygulamaya bildirilmez.
     */
    fun requestReview(activity: Activity)

    /** Uygulamanın Google Play sayfasını açar; Play Store yoksa tarayıcıda açar. */
    fun openStorePage(activity: Activity)
}

class PlayReviewController(context: Context) : ReviewController {
    private val manager: ReviewManager by lazy { ReviewManagerFactory.create(context) }

    override fun requestReview(activity: Activity) {
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (!request.isSuccessful) {
                Log.e(TAG, "Değerlendirme penceresi hazırlanamadı", request.exception)
                return@addOnCompleteListener
            }
            // Kullanıcı bu arada ekrandan çıktıysa pencere açılmaz.
            if (activity.isFinishing || activity.isDestroyed) return@addOnCompleteListener
            manager.launchReviewFlow(activity, request.result).addOnFailureListener { exception ->
                Log.e(TAG, "Değerlendirme penceresi açılamadı", exception)
            }
        }
    }

    override fun openStorePage(activity: Activity) {
        val id = activity.packageName
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$id".toUri()))
        } catch (exception: ActivityNotFoundException) {
            Log.w(TAG, "Play Store bulunamadı, sayfa tarayıcıda açılıyor", exception)
            try {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://play.google.com/store/apps/details?id=$id".toUri(),
                    ),
                )
            } catch (fallback: ActivityNotFoundException) {
                Log.e(TAG, "Google Play sayfası açılamadı", fallback)
            }
        }
    }

    private companion object {
        const val TAG = "ReviewController"
    }
}
