package com.zioanacleto.feedtracker.domain.repositories

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel

interface AuthRepository {
    suspend fun getAvailableAuthMethods(): List<AuthMethod>
    suspend fun startEmailRegistration(email: String)
    suspend fun verifyEmailCode(email: String, code: String): String
    suspend fun completeEmailRegistration(registrationToken: String, password: String, firstName: String, lastName: String): AuthSession
    suspend fun loginWithEmail(email: String, password: String): AuthSession
    suspend fun startPasswordReset(email: String)
    suspend fun verifyPasswordResetCode(email: String, code: String): String
    suspend fun resetPassword(resetToken: String, password: String): AuthSession
    suspend fun updateProfile(accessToken: String, firstName: String, lastName: String): UserModel
    suspend fun logout(accessToken: String)
}
