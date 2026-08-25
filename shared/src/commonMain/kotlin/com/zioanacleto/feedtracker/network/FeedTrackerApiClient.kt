package com.zioanacleto.feedtracker.network

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class FeedTrackerApiClient(val httpClient: HttpClient, private val baseUrl: String = getServerBaseUrl()) {
    private val trackingSessionsUrl = "$baseUrl/api/tracking-sessions"

    suspend fun getTrackingSessions(): List<TrackingSessionModel> = execute { httpClient.get(trackingSessionsUrl) }

    suspend fun getTrackingSession(id: String): TrackingSessionModel = execute { httpClient.get("$trackingSessionsUrl/$id") }

    suspend fun createTrackingSession(request: CreateTrackingSessionRequest): TrackingSessionModel = execute {
        httpClient.post(trackingSessionsUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    private suspend inline fun <reified T> execute(crossinline request: suspend () -> HttpResponse): T {
        val response = request()
        if (!response.status.isSuccess()) {
            val errorMessage = runCatching {
                response.body<ApiResponse<TrackingSessionModel>>().message
            }.getOrElse { response.status.description }
            throw ApiException(errorMessage)
        }

        val apiResponse = response.body<ApiResponse<T>>()
        if (apiResponse.status != SUCCESS_STATUS || apiResponse.data == null) {
            throw ApiException(apiResponse.message)
        }

        return apiResponse.data
    }

    companion object {
        const val SUCCESS_STATUS = "SUCCESS"
    }
}
