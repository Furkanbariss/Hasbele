package com.virata.hamdele.testing

import android.app.Activity
import com.virata.hamdele.core.data.prefs.ReviewStore
import com.virata.hamdele.core.review.ReviewController
import java.time.LocalDate

class FakeReviewController : ReviewController {
    var reviewRequests = 0
    var storeOpens = 0

    override fun requestReview(activity: Activity) {
        reviewRequests++
    }

    override fun openStorePage(activity: Activity) {
        storeOpens++
    }
}

class FakeReviewStore(var lastAsked: LocalDate? = null) : ReviewStore {
    override suspend fun lastAskedOn(): LocalDate? = lastAsked

    override suspend fun markAsked(date: LocalDate) {
        lastAsked = date
    }
}
