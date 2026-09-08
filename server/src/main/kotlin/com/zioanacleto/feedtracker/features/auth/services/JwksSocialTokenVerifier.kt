package com.zioanacleto.feedtracker.features.auth.services

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.zioanacleto.feedtracker.common.exceptions.UnauthorizedException
import com.zioanacleto.feedtracker.common.exceptions.ValidationException
import com.zioanacleto.feedtracker.config.AuthConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

class JwksSocialTokenVerifier(
    private val authConfig: AuthConfig,
    private val timeProvider: TimeProvider,
    private val googleKeys: () -> JWKSet = { JWKSet.load(URI(GOOGLE_JWKS).toURL()) },
    private val appleKeys: () -> JWKSet = { JWKSet.load(URI(APPLE_JWKS).toURL()) },
) : SocialTokenVerifier {

    @Volatile
    private var cachedGoogleKeys: CachedKeys? = null

    @Volatile
    private var cachedAppleKeys: CachedKeys? = null

    override suspend fun verifyGoogle(idToken: String): SocialProfile {
        if (authConfig.googleClientId.isBlank()) {
            throw ValidationException("Google Sign-In is not configured")
        }
        val claims = verify(
            token = idToken,
            expectedIssuer = GOOGLE_ISSUERS,
            expectedAudience = authConfig.googleClientId,
            keys = { googleKeySet() },
        )
        val email = claims.getStringClaim("email")?.trim().orEmpty()
        if (email.isBlank()) {
            throw UnauthorizedException("Google token did not include an email")
        }
        return SocialProfile(
            email = email,
            firstName = claims.getStringClaim("given_name").orEmpty(),
            lastName = claims.getStringClaim("family_name").orEmpty(),
        )
    }

    override suspend fun verifyApple(identityToken: String): SocialProfile {
        if (authConfig.appleAudience.isBlank()) {
            throw ValidationException("Apple Sign-In is not configured")
        }
        val claims = verify(
            token = identityToken,
            expectedIssuer = setOf(APPLE_ISSUER),
            expectedAudience = authConfig.appleAudience,
            keys = { appleKeySet() },
        )
        val email = claims.getStringClaim("email")?.trim().orEmpty()
        if (email.isBlank()) {
            throw UnauthorizedException("Apple token did not include an email")
        }
        return SocialProfile(email = email, firstName = "", lastName = "")
    }

    private suspend fun verify(token: String, expectedIssuer: Set<String>, expectedAudience: String, keys: () -> JWKSet): JWTClaimsSet =
        withContext(Dispatchers.IO) {
            val processor: ConfigurableJWTProcessor<SecurityContext> = DefaultJWTProcessor()
            val jwkSource: JWKSource<SecurityContext> = ImmutableJWKSet(keys())
            processor.jwsKeySelector = JWSVerificationKeySelector(JWSAlgorithm.RS256, jwkSource)
            val claims = runCatching { processor.process(token, null) }
                .getOrElse { throw UnauthorizedException("Invalid identity token") }
            val issuer = claims.issuer
            if (issuer !in expectedIssuer) {
                throw UnauthorizedException("Invalid identity token issuer")
            }
            if (claims.audience.none { it == expectedAudience }) {
                throw UnauthorizedException("Invalid identity token audience")
            }
            val expiration = claims.expirationTime?.time
                ?: throw UnauthorizedException("Invalid identity token")
            if (expiration <= timeProvider.nowMillis()) {
                throw UnauthorizedException("Identity token expired")
            }
            claims
        }

    private fun googleKeySet(): JWKSet {
        val now = timeProvider.nowMillis()
        val cached = cachedGoogleKeys
        if (cached != null && cached.expiresAt > now) return cached.keys
        val loaded = googleKeys()
        cachedGoogleKeys = CachedKeys(loaded, now + KEYS_TTL_MS)
        return loaded
    }

    private fun appleKeySet(): JWKSet {
        val now = timeProvider.nowMillis()
        val cached = cachedAppleKeys
        if (cached != null && cached.expiresAt > now) return cached.keys
        val loaded = appleKeys()
        cachedAppleKeys = CachedKeys(loaded, now + KEYS_TTL_MS)
        return loaded
    }

    private data class CachedKeys(val keys: JWKSet, val expiresAt: Long)

    companion object {
        private const val GOOGLE_JWKS = "https://www.googleapis.com/oauth2/v3/certs"
        private const val APPLE_JWKS = "https://appleid.apple.com/auth/keys"
        private const val APPLE_ISSUER = "https://appleid.apple.com"
        private const val KEYS_TTL_MS = 60L * 60L * 1000L
        private val GOOGLE_ISSUERS = setOf("https://accounts.google.com", "accounts.google.com")
    }
}
