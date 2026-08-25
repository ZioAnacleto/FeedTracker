package com.zioanacleto.feedtracker.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

actual fun createFeedTrackerHttpClient(): HttpClient = HttpClient(Android) {
    installFeedTrackerJson()
}
