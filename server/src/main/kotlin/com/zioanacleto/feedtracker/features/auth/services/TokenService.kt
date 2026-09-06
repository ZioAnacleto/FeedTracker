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

interface TokenService {
    fun createAccessToken(userId: String, ttlSeconds: Long): String
    fun createRegistrationToken(email: String, ttlSeconds: Long): String
    fun parseRegistrationToken(token: String): String
}

class JwtTokenService(secret: String, private val timeProvider: TimeProvider) : TokenService {
    private val secretBytes = normalizeSecret(secret)
    private val signer = MACSigner(secretBytes)
    private val verifier = MACVerifier(secretBytes)

    override fun createAccessToken(userId: String, ttlSeconds: Long): String =
        signedToken(subject = userId, type = ACCESS, ttlSeconds = ttlSeconds)

    override fun createRegistrationToken(email: String, ttlSeconds: Long): String =
        signedToken(subject = email, type = REGISTRATION, ttlSeconds = ttlSeconds)

    override fun parseRegistrationToken(token: String): String {
        val jwt = parseAndVerify(token)
        val claims = jwt.jwtClaimsSet
        if (claims.getStringClaim(TYPE_CLAIM) != REGISTRATION) {
            throw UnauthorizedException("Invalid registration token")
        }
        return claims.subject ?: throw UnauthorizedException("Invalid registration token")
    }

    private fun signedToken(subject: String, type: String, ttlSeconds: Long): String {
        val now = timeProvider.nowMillis()
        val claims = JWTClaimsSet.Builder()
            .subject(subject)
            .claim(TYPE_CLAIM, type)
            .issueTime(Date(now))
            .expirationTime(Date(now + ttlSeconds * 1000))
            .build()
        val jwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
            claims,
        )
        jwt.sign(signer)
        return jwt.serialize()
    }

    private fun parseAndVerify(token: String): SignedJWT {
        val jwt = runCatching { SignedJWT.parse(token) }
            .getOrElse { throw UnauthorizedException("Invalid registration token") }
        if (!jwt.verify(verifier)) {
            throw UnauthorizedException("Invalid registration token")
        }
        val expiration = jwt.jwtClaimsSet.expirationTime?.time
            ?: throw UnauthorizedException("Invalid registration token")
        if (expiration <= timeProvider.nowMillis()) {
            throw UnauthorizedException("Registration token expired")
        }
        return jwt
    }

    companion object {
        private const val TYPE_CLAIM = "typ"
        private const val ACCESS = "access"
        private const val REGISTRATION = "email_registration"
        private const val MIN_SECRET_BYTES = 32

        private fun normalizeSecret(secret: String): ByteArray {
            val bytes = secret.toByteArray(StandardCharsets.UTF_8)
            if (bytes.size >= MIN_SECRET_BYTES) return bytes
            return bytes.copyOf(MIN_SECRET_BYTES)
        }
    }
}
