package com.zioanacleto.feedtracker.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class StartEmailAuthRequest(val email: String)

@Serializable
data class VerifyEmailCodeRequest(val email: String, val code: String)

@Serializable
data class VerifyEmailCodeResponse(val registrationToken: String)

@Serializable
data class CompleteEmailRegistrationRequest(
    val registrationToken: String,
    val password: String,
    val firstName: String,
    val lastName: String,
)

@Serializable
data class EmailLoginRequest(val email: String, val password: String)

@Serializable
data class SocialLoginRequest(val idToken: String, val firstName: String? = null, val lastName: String? = null)

@Serializable
data class AuthMethodsResponse(val methods: List<AuthMethod>)

@Serializable
data class UpdateProfileRequest(val firstName: String, val lastName: String)

@Serializable
data class ResetPasswordRequest(val resetToken: String, val password: String)

@Serializable
data class VerifyPasswordResetResponse(val resetToken: String)
