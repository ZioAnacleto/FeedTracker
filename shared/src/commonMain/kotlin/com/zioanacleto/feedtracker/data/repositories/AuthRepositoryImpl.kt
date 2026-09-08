package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.CompleteEmailRegistrationRequest
import com.zioanacleto.feedtracker.domain.auth.EmailLoginRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.UpdateProfileRequest
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.network.FeedTrackerApiClient

class AuthRepositoryImpl(private val apiClient: FeedTrackerApiClient) : AuthRepository {
    override suspend fun getAvailableAuthMethods(): List<AuthMethod> = apiClient.getAvailableAuthMethods().methods

    override suspend fun startEmailRegistration(email: String) {
        apiClient.startEmailRegistration(StartEmailAuthRequest(email = email.trim()))
    }

    override suspend fun verifyEmailCode(email: String, code: String): String =
        apiClient.verifyEmailCode(VerifyEmailCodeRequest(email = email.trim(), code = code.trim())).registrationToken

    override suspend fun completeEmailRegistration(
        registrationToken: String,
        password: String,
        firstName: String,
        lastName: String,
    ): AuthSession = apiClient.completeEmailRegistration(
        CompleteEmailRegistrationRequest(
            registrationToken = registrationToken,
            password = password,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
        ),
    )

    override suspend fun loginWithEmail(email: String, password: String): AuthSession =
        apiClient.loginWithEmail(EmailLoginRequest(email = email.trim(), password = password))

    override suspend fun updateProfile(accessToken: String, firstName: String, lastName: String): UserModel = apiClient.updateProfile(
        accessToken = accessToken,
        request = UpdateProfileRequest(firstName = firstName.trim(), lastName = lastName.trim()),
    )

    override suspend fun logout(accessToken: String) {
        apiClient.logout(accessToken)
    }
}
