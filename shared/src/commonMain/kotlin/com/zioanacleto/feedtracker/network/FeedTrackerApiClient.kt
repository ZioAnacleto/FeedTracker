package com.zioanacleto.feedtracker.network

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.auth.AuthMethodsResponse
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.CompleteEmailRegistrationRequest
import com.zioanacleto.feedtracker.domain.auth.EmailLoginRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class FeedTrackerApiClient(val httpClient: HttpClient, private val baseUrl: String = getServerBaseUrl()) {
    private val trackingSessionsUrl = "$baseUrl/api/tracking-sessions"
    private val authUrl = "$baseUrl/api/auth"

    suspend fun getAvailableAuthMethods(): AuthMethodsResponse = execute { httpClient.get("$authUrl/methods") }

    suspend fun startEmailRegistration(request: StartEmailAuthRequest) {
        executeNoContent {
            httpClient.post("$authUrl/email/start") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun verifyEmailCode(request: VerifyEmailCodeRequest): VerifyEmailCodeResponse = execute {
        httpClient.post("$authUrl/email/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun completeEmailRegistration(request: CompleteEmailRegistrationRequest): AuthSession = execute {
        httpClient.post("$authUrl/email/complete") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun loginWithEmail(request: EmailLoginRequest): AuthSession = execute {
        httpClient.post("$authUrl/email/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun logout(accessToken: String) {
        executeNoContent {
            httpClient.post("$authUrl/logout") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
    }

    suspend fun getTrackingSessions(): List<TrackingSessionModel> = execute { httpClient.get(trackingSessionsUrl) }

    suspend fun getTrackingSession(id: String): TrackingSessionModel = execute { httpClient.get("$trackingSessionsUrl/$id") }

    suspend fun createTrackingSession(request: CreateTrackingSessionRequest): TrackingSessionModel = execute {
        httpClient.post(trackingSessionsUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteTrackingSession(id: String) {
        executeNoContent { httpClient.delete("$trackingSessionsUrl/$id") }
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

    private suspend inline fun executeNoContent(crossinline request: suspend () -> HttpResponse) {
        val response = request()
        if (!response.status.isSuccess()) {
            val errorMessage = runCatching {
                response.body<ApiResponse<String?>>().message
            }.getOrElse { response.status.description }
            throw ApiException(errorMessage)
        }

        val apiResponse = response.body<ApiResponse<String?>>()
        if (apiResponse.status != SUCCESS_STATUS) {
            throw ApiException(apiResponse.message)
        }
    }

    companion object {
        const val SUCCESS_STATUS = "SUCCESS"
    }
}
