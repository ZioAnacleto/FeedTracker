package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCode

interface EmailVerificationRepository {
    suspend fun replaceActiveCode(email: String, codeHash: String, expiresAt: Long, createdAt: Long): EmailVerificationCode
    suspend fun findActiveByEmail(email: String): EmailVerificationCode?
    suspend fun incrementAttempts(id: String)
    suspend fun consume(id: String, consumedAt: Long)
}
