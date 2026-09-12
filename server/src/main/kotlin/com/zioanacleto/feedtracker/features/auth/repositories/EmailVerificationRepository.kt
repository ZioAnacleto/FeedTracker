package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCode
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationPurpose

interface EmailVerificationRepository {
    suspend fun replaceActiveCode(
        email: String,
        codeHash: String,
        expiresAt: Long,
        createdAt: Long,
        purpose: String = EmailVerificationPurpose.REGISTRATION,
    ): EmailVerificationCode

    suspend fun findActiveByEmail(email: String, purpose: String = EmailVerificationPurpose.REGISTRATION): EmailVerificationCode?

    suspend fun incrementAttempts(id: String)
    suspend fun consume(id: String, consumedAt: Long)
}
