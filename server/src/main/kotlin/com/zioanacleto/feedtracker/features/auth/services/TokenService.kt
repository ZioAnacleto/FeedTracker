package com.zioanacleto.feedtracker.features.auth.services

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.zioanacleto.feedtracker.common.exceptions.UnauthorizedException
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID

data class AccessTokenClaims(val userId: String, val jti: String, val expiresAtMillis: Long)

interface TokenService {
    fun createAccessToken(userId: String, ttlSeconds: Long): String
    fun createRegistrationToken(email: String, ttlSeconds: Long): String
    fun parseRegistrationToken(token: String): String
    fun parseAccessToken(token: String): AccessTokenClaims
}

class JwtTokenService(secret: String, private val timeProvider: TimeProvider) : TokenService {
    private val secretBytes = normalizeSecret(secret)
    private val signer = MACSigner(secretBytes)
    private val verifier = MACVerifier(secretBytes)

    override fun createAccessToken(userId: String, ttlSeconds: Long): String =
        signedToken(subject = userId, type = ACCESS, ttlSeconds = ttlSeconds, jwtId = UUID.randomUUID().toString())

    override fun createRegistrationToken(email: String, ttlSeconds: Long): String =
        signedToken(subject = email, type = REGISTRATION, ttlSeconds = ttlSeconds)

    override fun parseRegistrationToken(token: String): String {
        val claims = parseAndVerify(token, invalidMessage = INVALID_REGISTRATION, expiredMessage = EXPIRED_REGISTRATION)
        if (claims.getStringClaim(TYPE_CLAIM) != REGISTRATION) {
            throw UnauthorizedException(INVALID_REGISTRATION)
        }
        return claims.subject ?: throw UnauthorizedException(INVALID_REGISTRATION)
    }

    override fun parseAccessToken(token: String): AccessTokenClaims {
        val claims = parseAndVerify(token, invalidMessage = INVALID_ACCESS, expiredMessage = EXPIRED_ACCESS)
        if (claims.getStringClaim(TYPE_CLAIM) != ACCESS) {
            throw UnauthorizedException(INVALID_ACCESS)
        }
        val userId = claims.subject ?: throw UnauthorizedException(INVALID_ACCESS)
        val jti = claims.jwtid ?: throw UnauthorizedException(INVALID_ACCESS)
        val expiresAt = claims.expirationTime?.time ?: throw UnauthorizedException(INVALID_ACCESS)
        return AccessTokenClaims(userId = userId, jti = jti, expiresAtMillis = expiresAt)
    }

    private fun signedToken(subject: String, type: String, ttlSeconds: Long, jwtId: String? = null): String {
        val now = timeProvider.nowMillis()
        val claimsBuilder = JWTClaimsSet.Builder()
            .subject(subject)
            .claim(TYPE_CLAIM, type)
            .issueTime(Date(now))
            .expirationTime(Date(now + ttlSeconds * 1000))
        if (jwtId != null) {
            claimsBuilder.jwtID(jwtId)
        }
        val claims = claimsBuilder.build()
        val jwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
            claims,
        )
        jwt.sign(signer)
        return jwt.serialize()
    }

    private fun parseAndVerify(token: String, invalidMessage: String, expiredMessage: String): JWTClaimsSet {
        val jwt = runCatching { SignedJWT.parse(token) }
            .getOrElse { throw UnauthorizedException(invalidMessage) }
        if (!jwt.verify(verifier)) {
            throw UnauthorizedException(invalidMessage)
        }
        val claims = jwt.jwtClaimsSet
        val expiration = claims.expirationTime?.time ?: throw UnauthorizedException(invalidMessage)
        if (expiration <= timeProvider.nowMillis()) {
            throw UnauthorizedException(expiredMessage)
        }
        return claims
    }

    companion object {
        private const val TYPE_CLAIM = "typ"
        private const val ACCESS = "access"
        private const val REGISTRATION = "email_registration"
        private const val INVALID_REGISTRATION = "Invalid registration token"
        private const val EXPIRED_REGISTRATION = "Registration token expired"
        private const val INVALID_ACCESS = "Invalid access token"
        private const val EXPIRED_ACCESS = "Access token expired"
        private const val MIN_SECRET_BYTES = 32

        private fun normalizeSecret(secret: String): ByteArray {
            val bytes = secret.toByteArray(StandardCharsets.UTF_8)
            if (bytes.size >= MIN_SECRET_BYTES) return bytes
            return bytes.copyOf(MIN_SECRET_BYTES)
        }
    }
}
