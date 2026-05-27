package com.zioanacleto.feedtracker.network

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class FeedTrackerApiClient(
    val httpClient: HttpClient,
    private val baseUrl: String = getServerBaseUrl(),
) {
    suspend inline fun <reified Request : Any, Response : Any> createPostRequest(
        url: String,
        request: Request
    ): Response {
        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            val errorMessage = runCatching {
                response.body<ApiResponse<Nothing>>().message
            }.getOrElse { response.status.description }
            throw ApiException(errorMessage)
        }

        val apiResponse = response.body<ApiResponse<Response>>()
        if (apiResponse.status != SUCCESS_STATUS || apiResponse.data == null) {
            throw ApiException(apiResponse.message)
        }

        return apiResponse.data
    }

    companion object {
        const val SUCCESS_STATUS = "SUCCESS"
    }
}
