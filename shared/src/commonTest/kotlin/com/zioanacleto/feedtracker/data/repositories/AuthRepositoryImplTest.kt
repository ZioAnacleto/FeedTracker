package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthMethodsResponse
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeResponse
import com.zioanacleto.feedtracker.network.FeedTrackerApiClient
import com.zioanacleto.feedtracker.network.installFeedTrackerJson
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

class AuthRepositoryImplTest {

    private val json = Json { encodeDefaults = true }
    private val session = AuthSession(
        user = UserModel(
            id = "user-1",
            email = "mario@example.com",
            authMethods = listOf(AuthMethod.EMAIL),
            firstName = "Mario",
            lastName = "Rossi",
        ),
        accessToken = "access-token",
    )

    @Test
    fun getAvailableAuthMethodsReadsServerCatalog() = runTest {
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Get
            request.url.encodedPath shouldBe "/api/auth/methods"
            respond(
                content = ByteReadChannel(
                    json.encodeToString(
                        ApiResponse(
                            status = "SUCCESS",
                            message = "ok",
                            data = AuthMethodsResponse(listOf(AuthMethod.EMAIL, AuthMethod.APPLE)),
                        ),
                    ),
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = AuthRepositoryImpl(apiClient(engine))

        repository.getAvailableAuthMethods() shouldBe listOf(AuthMethod.EMAIL, AuthMethod.APPLE)
    }

    @Test
    fun loginWithEmailPostsCredentials() = runTest {
        var posted = false
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.encodedPath shouldBe "/api/auth/email/login"
            posted = true
            respond(
                content = ByteReadChannel(json.encodeToString(ApiResponse("SUCCESS", "ok", session))),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = AuthRepositoryImpl(apiClient(engine))

        repository.loginWithEmail(" mario@example.com ", "password1") shouldBe session
        posted shouldBe true
    }

    @Test
    fun startEmailRegistrationPostsToEmailStart() = runTest {
        var posted = false
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.encodedPath shouldBe "/api/auth/email/start"
            posted = true
            respond(
                content = ByteReadChannel(json.encodeToString(ApiResponse<String?>("SUCCESS", "Verification email sent"))),
                status = HttpStatusCode.Accepted,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = AuthRepositoryImpl(apiClient(engine))

        repository.startEmailRegistration(" mario@example.com ")
        posted shouldBe true
    }

    @Test
    fun verifyEmailCodePostsToEmailVerify() = runTest {
        var posted = false
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.encodedPath shouldBe "/api/auth/email/verify"
            posted = true
            respond(
                content = ByteReadChannel(
                    json.encodeToString(
                        ApiResponse(
                            status = "SUCCESS",
                            message = "ok",
                            data = VerifyEmailCodeResponse("reg-token"),
                        ),
                    ),
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = AuthRepositoryImpl(apiClient(engine))

        repository.verifyEmailCode("mario@example.com", "123456") shouldBe "reg-token"
        posted shouldBe true
    }

    @Test
    fun completeEmailRegistrationPostsToEmailComplete() = runTest {
        var posted = false
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.encodedPath shouldBe "/api/auth/email/complete"
            posted = true
            respond(
                content = ByteReadChannel(json.encodeToString(ApiResponse("SUCCESS", "ok", session))),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = AuthRepositoryImpl(apiClient(engine))

        repository.completeEmailRegistration("reg-token", "password1", "Mario", "Rossi") shouldBe session
        posted shouldBe true
    }

    @Test
    fun logoutPostsBearerToken() = runTest {
        var authorization: String? = null
        val engine = MockEngine { request ->
            request.method shouldBe HttpMethod.Post
            request.url.encodedPath shouldBe "/api/auth/logout"
            authorization = request.headers[HttpHeaders.Authorization]
            respond(
                content = ByteReadChannel(json.encodeToString(ApiResponse<String?>("SUCCESS", "Logged out"))),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = AuthRepositoryImpl(apiClient(engine))

        repository.logout("access-token")
        authorization shouldBe "Bearer access-token"
    }

    private fun apiClient(engine: MockEngine) = FeedTrackerApiClient(
        httpClient = HttpClient(engine) { installFeedTrackerJson() },
        baseUrl = "http://test-host:8080",
    )
}
