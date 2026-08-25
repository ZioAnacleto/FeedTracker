package com.zioanacleto.feedtracker.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createFeedTrackerHttpClient(): HttpClient = HttpClient(CIO) {
    installFeedTrackerJson()
}
