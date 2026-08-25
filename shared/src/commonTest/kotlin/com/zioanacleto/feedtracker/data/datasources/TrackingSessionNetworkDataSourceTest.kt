package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.network.FeedTrackerApiClient
import com.zioanacleto.feedtracker.network.installFeedTrackerJson
import com.zioanacleto.feedtracker.testutil.trackingSession
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class TrackingSessionNetworkDataSourceTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun saveNewTrackingSessionPostsCreateRequest() = runTest {
        val session = trackingSession().copy(additionalNotes = "note")
        var posted = false
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.encodedPath shouldBe "/api/tracking-sessions"
            posted = true
            respond(
                content = ByteReadChannel(json.encodeToString(ApiResponse("SUCCESS", "ok", session))),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val dataSource = TrackingSessionNetworkDataSource(
            FeedTrackerApiClient(
                httpClient = HttpClient(engine) { installFeedTrackerJson() },
                baseUrl = "http://test-host:8080",
            ),
        )

        dataSource.saveNewTrackingSession(session)

        posted shouldBe true
    }
}
