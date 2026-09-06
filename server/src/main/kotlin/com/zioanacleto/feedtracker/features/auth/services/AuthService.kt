package com.zioanacleto.feedtracker.features.auth.services

import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.CompleteEmailRegistrationRequest
import com.zioanacleto.feedtracker.domain.auth.EmailLoginRequest
import com.zioanacleto.feedtracker.domain.auth.SocialLoginRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeResponse

interface AuthService {
    suspend fun startEmailRegistration(request: StartEmailAuthRequest)
    suspend fun verifyEmailCode(request: VerifyEmailCodeRequest): VerifyEmailCodeResponse
    suspend fun completeEmailRegistration(request: CompleteEmailRegistrationRequest): AuthSession
    suspend fun loginWithEmail(request: EmailLoginRequest): AuthSession
    suspend fun loginWithGoogle(request: SocialLoginRequest): AuthSession
    suspend fun loginWithApple(request: SocialLoginRequest): AuthSession
}
