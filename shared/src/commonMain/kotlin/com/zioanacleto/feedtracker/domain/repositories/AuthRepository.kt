package com.zioanacleto.feedtracker.domain.repositories

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession

interface AuthRepository {
    suspend fun getAvailableAuthMethods(): List<AuthMethod>
    suspend fun startEmailRegistration(email: String)
    suspend fun verifyEmailCode(email: String, code: String): String
    suspend fun completeEmailRegistration(registrationToken: String, password: String, firstName: String, lastName: String): AuthSession
    suspend fun loginWithEmail(email: String, password: String): AuthSession
    suspend fun logout(accessToken: String)
}
