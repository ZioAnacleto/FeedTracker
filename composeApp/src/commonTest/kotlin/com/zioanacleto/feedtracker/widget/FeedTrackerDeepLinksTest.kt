package com.zioanacleto.feedtracker.widget

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FeedTrackerDeepLinksTest {

    @Test
    fun recognizesNewTrackingUri() {
        FeedTrackerDeepLinks.isNewTracking("feedtracker://new-tracking") shouldBe true
        FeedTrackerDeepLinks.isNewTracking("feedtracker://new-tracking/") shouldBe true
        FeedTrackerDeepLinks.isNewTracking("feedtracker://new-tracking?from=widget") shouldBe true
        FeedTrackerDeepLinks.isNewTracking(" feedtracker://new-tracking ") shouldBe true
        FeedTrackerDeepLinks.isNewTracking("feedtracker://home") shouldBe false
        FeedTrackerDeepLinks.isNewTracking("https://new-tracking") shouldBe false
        FeedTrackerDeepLinks.isNewTracking(null) shouldBe false
        FeedTrackerDeepLinks.isNewTracking("") shouldBe false
    }
}
