package com.zioanacleto.feedtracker.features.auth.repositories

interface RevokedAccessTokenRepository {
    suspend fun revoke(jti: String, expiresAt: Long, revokedAt: Long)
    suspend fun isRevoked(jti: String): Boolean
}
