package com.zioanacleto.feedtracker.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createFeedTrackerHttpClient(): HttpClient = HttpClient(Darwin) {
    installFeedTrackerJson()
}
