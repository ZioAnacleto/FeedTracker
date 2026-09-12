package com.zioanacleto.feedtracker.network

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.ResetPasswordRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyPasswordResetResponse
import com.zioanacleto.feedtracker.testutil.trackingSession
import io.kotest.assertions.throwables.shouldThrow
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

class FeedTrackerApiClientTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun getTrackingSessionsReturnsPayloadOnSuccess() = runTest {
        val session = trackingSession()
        val client = apiClient(
            expectedMethod = HttpMethod.Get,
            expectedPath = "/api/tracking-sessions",
            body = json.encodeToString(ApiResponse("SUCCESS", "ok", listOf(session))),
        )

        client.getTrackingSessions() shouldBe listOf(session)
    }

    @Test
    fun getTrackingSessionReturnsPayloadOnSuccess() = runTest {
        val session = trackingSession()
        val client = apiClient(
            expectedMethod = HttpMethod.Get,
            expectedPath = "/api/tracking-sessions/session-1",
            body = json.encodeToString(ApiResponse("SUCCESS", "ok", session)),
        )

        client.getTrackingSession("session-1") shouldBe session
    }

    @Test
    fun createTrackingSessionPostsJsonBody() = runTest {
        val session = trackingSession()
        val client = apiClient(
            expectedMethod = HttpMethod.Post,
            expectedPath = "/api/tracking-sessions",
            body = json.encodeToString(ApiResponse("SUCCESS", "created", session)),
        )

        val created = client.createTrackingSession(
            CreateTrackingSessionRequest(
                sessionStartTime = session.sessionStartTime,
                sessionEndTime = session.sessionEndTime,
                name = session.name,
                surname = session.surname,
                birthDate = session.birthDate,
                additionalNotes = session.additionalNotes,
            ),
        )

        created shouldBe session
    }

    @Test
    fun deleteTrackingSessionSucceedsWhenPayloadHasNoData() = runTest {
        val client = apiClient(
            expectedMethod = HttpMethod.Delete,
            expectedPath = "/api/tracking-sessions/session-1",
            body = json.encodeToString(ApiResponse<Unit>(status = "SUCCESS", message = "deleted")),
        )

        client.deleteTrackingSession("session-1")
    }

    @Test
    fun startPasswordResetPostsToForgotPassword() = runTest {
        val client = apiClient(
            expectedMethod = HttpMethod.Post,
            expectedPath = "/api/auth/email/forgot-password",
            status = HttpStatusCode.Accepted,
            body = json.encodeToString(ApiResponse<String?>("SUCCESS", "sent")),
        )

        client.startPasswordReset(StartEmailAuthRequest("mario@example.com"))
    }

    @Test
    fun verifyPasswordResetCodeReturnsToken() = runTest {
        val client = apiClient(
            expectedMethod = HttpMethod.Post,
            expectedPath = "/api/auth/email/reset/verify",
            body = json.encodeToString(
                ApiResponse(
                    status = "SUCCESS",
                    message = "ok",
                    data = VerifyPasswordResetResponse("reset-token"),
                ),
            ),
        )

        client.verifyPasswordResetCode(VerifyEmailCodeRequest("mario@example.com", "123456"))
            .resetToken shouldBe "reset-token"
    }

    @Test
    fun resetPasswordReturnsSession() = runTest {
        val session = AuthSession(
            user = UserModel(
                id = "user-1",
                email = "mario@example.com",
                authMethods = listOf(AuthMethod.EMAIL),
                firstName = "Mario",
                lastName = "Rossi",
            ),
            accessToken = "access-token",
        )
        val client = apiClient(
            expectedMethod = HttpMethod.Post,
            expectedPath = "/api/auth/email/reset",
            body = json.encodeToString(ApiResponse("SUCCESS", "ok", session)),
        )

        client.resetPassword(ResetPasswordRequest("reset-token", "password2")) shouldBe session
    }

    @Test
    fun throwsApiExceptionWhenHttpStatusIsNotSuccessful() = runTest {
        val client = apiClient(
            expectedMethod = HttpMethod.Get,
            expectedPath = "/api/tracking-sessions/missing",
            status = HttpStatusCode.NotFound,
            body = json.encodeToString(
                ApiResponse<Unit>(status = "ERROR", message = "Tracking session not found", data = null),
            ),
        )

        val error = shouldThrow<ApiException> { client.getTrackingSession("missing") }
        error.message shouldBe "Tracking session not found"
    }

    @Test
    fun throwsApiExceptionWhenPayloadStatusIsNotSuccess() = runTest {
        val client = apiClient(
            expectedMethod = HttpMethod.Get,
            expectedPath = "/api/tracking-sessions",
            body = json.encodeToString(
                ApiResponse<List<com.zioanacleto.feedtracker.domain.TrackingSessionModel>>(
                    status = "ERROR",
                    message = "backend failed",
                    data = null,
                ),
            ),
        )

        val error = shouldThrow<ApiException> { client.getTrackingSessions() }
        error.message shouldBe "backend failed"
    }

    private fun apiClient(
        expectedMethod: HttpMethod,
        expectedPath: String,
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): FeedTrackerApiClient {
        val engine = MockEngine { request ->
            request.method shouldBe expectedMethod
            request.url.encodedPath shouldBe expectedPath
            respond(
                content = ByteReadChannel(body),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        return FeedTrackerApiClient(
            httpClient = HttpClient(engine) { installFeedTrackerJson() },
            baseUrl = "http://test-host:8080",
        )
    }
}
