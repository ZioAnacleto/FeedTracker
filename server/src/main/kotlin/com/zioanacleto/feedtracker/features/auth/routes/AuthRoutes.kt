package com.zioanacleto.feedtracker.features.auth.routes

import com.zioanacleto.feedtracker.common.exceptions.UnauthorizedException
import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.domain.auth.AuthMethodsResponse
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.CompleteEmailRegistrationRequest
import com.zioanacleto.feedtracker.domain.auth.EmailLoginRequest
import com.zioanacleto.feedtracker.domain.auth.SocialLoginRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeResponse
import com.zioanacleto.feedtracker.features.auth.services.AuthService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        get("/methods") {
            call.respond(
                ApiResponse(
                    status = "SUCCESS",
                    message = "Available auth methods",
                    data = AuthMethodsResponse(authService.availableAuthMethods()),
                ),
            )
        }

        post("/email/start") {
            val request = call.receive<StartEmailAuthRequest>()
            authService.startEmailRegistration(request)
            call.respond(
                HttpStatusCode.Accepted,
                ApiResponse<Unit>("SUCCESS", "Verification email sent"),
            )
        }

        post("/email/verify") {
            val request = call.receive<VerifyEmailCodeRequest>()
            val result = authService.verifyEmailCode(request)
            call.respond(
                ApiResponse<VerifyEmailCodeResponse>("SUCCESS", "Email verified", result),
            )
        }

        post("/email/complete") {
            val request = call.receive<CompleteEmailRegistrationRequest>()
            val session = authService.completeEmailRegistration(request)
            call.respond(
                HttpStatusCode.Created,
                ApiResponse<AuthSession>("SUCCESS", "Account created", session),
            )
        }

        post("/email/login") {
            val request = call.receive<EmailLoginRequest>()
            val session = authService.loginWithEmail(request)
            call.respond(ApiResponse<AuthSession>("SUCCESS", "Login successful", session))
        }

        post("/google") {
            val request = call.receive<SocialLoginRequest>()
            val session = authService.loginWithGoogle(request)
            call.respond(ApiResponse<AuthSession>("SUCCESS", "Login successful", session))
        }

        post("/apple") {
            val request = call.receive<SocialLoginRequest>()
            val session = authService.loginWithApple(request)
            call.respond(ApiResponse<AuthSession>("SUCCESS", "Login successful", session))
        }

        post("/logout") {
            authService.logout(call.bearerToken())
            call.respond(ApiResponse<Unit>("SUCCESS", "Logged out"))
        }
    }
}

private fun ApplicationCall.bearerToken(): String {
    val header = request.header(HttpHeaders.Authorization)?.trim().orEmpty()
    if (!header.startsWith("Bearer ", ignoreCase = true)) {
        throw UnauthorizedException("Missing access token")
    }
    return header.substringAfter(' ').trim().ifBlank {
        throw UnauthorizedException("Missing access token")
    }
}
