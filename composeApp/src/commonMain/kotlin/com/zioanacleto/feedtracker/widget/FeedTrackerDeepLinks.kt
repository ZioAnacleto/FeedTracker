package com.zioanacleto.feedtracker.widget

object FeedTrackerDeepLinks {
    const val SCHEME = "feedtracker"
    const val HOST_NEW_TRACKING = "new-tracking"
    const val NEW_TRACKING_URI = "$SCHEME://$HOST_NEW_TRACKING"

    fun isNewTracking(uri: String?): Boolean {
        if (uri.isNullOrBlank()) return false
        val normalized = uri.trim().trimEnd('/')
        return normalized == NEW_TRACKING_URI ||
            normalized.startsWith("$NEW_TRACKING_URI?")
    }
}
