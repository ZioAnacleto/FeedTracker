package com.zioanacleto.feedtracker.features.auth.services

import com.zioanacleto.feedtracker.common.exceptions.ApplicationException
import com.zioanacleto.feedtracker.common.exceptions.ConflictException
import com.zioanacleto.feedtracker.common.exceptions.UnauthorizedException
import com.zioanacleto.feedtracker.common.exceptions.ValidationException
import com.zioanacleto.feedtracker.config.AuthConfig
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.CompleteEmailRegistrationRequest
import com.zioanacleto.feedtracker.domain.auth.EmailLoginRequest
import com.zioanacleto.feedtracker.domain.auth.SocialLoginRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeResponse
import com.zioanacleto.feedtracker.features.auth.models.NewUser
import com.zioanacleto.feedtracker.features.auth.repositories.EmailVerificationRepository
import com.zioanacleto.feedtracker.features.auth.repositories.RevokedAccessTokenRepository
import com.zioanacleto.feedtracker.features.auth.repositories.UserRepository
import io.ktor.http.HttpStatusCode
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.HexFormat
import java.util.UUID

class AuthServiceImpl(
    private val users: UserRepository,
    private val verifications: EmailVerificationRepository,
    private val emailSender: EmailSender,
    private val passwordHasher: PasswordHasher,
    private val tokens: TokenService,
    private val codes: VerificationCodeGenerator,
    private val socialVerifier: SocialTokenVerifier,
    private val timeProvider: TimeProvider,
    private val authConfig: AuthConfig,
    private val revokedTokens: RevokedAccessTokenRepository,
) : AuthService {

    override fun availableAuthMethods(): List<AuthMethod> = buildList {
        add(AuthMethod.EMAIL)
        if (authConfig.googleClientId.isNotBlank()) add(AuthMethod.GOOGLE)
        if (authConfig.appleAudience.isNotBlank()) add(AuthMethod.APPLE)
    }

    override suspend fun startEmailRegistration(request: StartEmailAuthRequest) {
        val email = normalizeEmail(request.email)
        validateEmail(email)
        if (users.findByEmail(email)?.passwordHash != null) {
            throw ConflictException("An account already exists for this email")
        }
        val now = timeProvider.nowMillis()
        val code = codes.generate()
        verifications.replaceActiveCode(
            email = email,
            codeHash = hashVerificationCode(email, code),
            expiresAt = now + authConfig.verificationCodeTtlSeconds * 1000,
            createdAt = now,
        )
        val link = buildVerificationLink(email, code)
        runCatching { emailSender.sendVerification(email, code, link) }
            .getOrElse {
                throw ApplicationException(
                    "Unable to send verification email",
                    HttpStatusCode.BadGateway,
                )
            }
    }

    override suspend fun verifyEmailCode(request: VerifyEmailCodeRequest): VerifyEmailCodeResponse {
        val email = normalizeEmail(request.email)
        validateEmail(email)
        val code = request.code.trim()
        if (code.length != CODE_LENGTH || code.any { !it.isDigit() }) {
            throw ValidationException("Verification code must be 6 digits")
        }
        val active = verifications.findActiveByEmail(email)
            ?: throw UnauthorizedException(INVALID_CODE)
        val now = timeProvider.nowMillis()
        if (active.expiresAt <= now || active.attemptCount >= MAX_CODE_ATTEMPTS) {
            throw UnauthorizedException(INVALID_CODE)
        }
        val expected = hashVerificationCode(email, code).toByteArray(StandardCharsets.UTF_8)
        val actual = active.codeHash.toByteArray(StandardCharsets.UTF_8)
        if (!MessageDigest.isEqual(expected, actual)) {
            verifications.incrementAttempts(active.id)
            throw UnauthorizedException(INVALID_CODE)
        }
        verifications.consume(active.id, now)
        return VerifyEmailCodeResponse(
            registrationToken = tokens.createRegistrationToken(email, authConfig.registrationTokenTtlSeconds),
        )
    }

    override suspend fun completeEmailRegistration(request: CompleteEmailRegistrationRequest): AuthSession {
        val email = tokens.parseRegistrationToken(request.registrationToken)
        validateName(request.firstName, "firstName")
        validateName(request.lastName, "lastName")
        validatePassword(request.password)
        val existing = users.findByEmail(email)
        if (existing != null) {
            if (existing.passwordHash != null) {
                throw ConflictException("An account already exists for this email")
            }
            val user = users.addAuthMethod(
                userId = existing.model.id,
                method = AuthMethod.EMAIL,
                passwordHash = passwordHasher.hash(request.password),
            )
            return sessionFor(user)
        }
        val user = users.create(
            NewUser(
                id = UUID.randomUUID().toString(),
                email = email,
                passwordHash = passwordHasher.hash(request.password),
                authMethod = AuthMethod.EMAIL.name,
                firstName = request.firstName.trim(),
                lastName = request.lastName.trim(),
                createdAt = timeProvider.nowMillis(),
            ),
        )
        return sessionFor(user)
    }

    override suspend fun loginWithEmail(request: EmailLoginRequest): AuthSession {
        val email = normalizeEmail(request.email)
        val stored = users.findByEmail(email) ?: throw UnauthorizedException(INVALID_CREDENTIALS)
        val passwordHash = stored.passwordHash ?: throw UnauthorizedException(INVALID_CREDENTIALS)
        if (!passwordHasher.verify(request.password, passwordHash)) {
            throw UnauthorizedException(INVALID_CREDENTIALS)
        }
        return sessionFor(stored.model)
    }

    override suspend fun loginWithGoogle(request: SocialLoginRequest): AuthSession {
        val profile = socialVerifier.verifyGoogle(request.idToken)
        return upsertSocialUser(
            profile = profile.copy(
                firstName = profile.firstName.ifBlank { request.firstName.orEmpty() },
                lastName = profile.lastName.ifBlank { request.lastName.orEmpty() },
            ),
            method = AuthMethod.GOOGLE,
        )
    }

    override suspend fun logout(accessToken: String) {
        val claims = tokens.parseAccessToken(accessToken)
        revokedTokens.revoke(
            jti = claims.jti,
            expiresAt = claims.expiresAtMillis,
            revokedAt = timeProvider.nowMillis(),
        )
    }

    override suspend fun loginWithApple(request: SocialLoginRequest): AuthSession {
        val profile = socialVerifier.verifyApple(request.idToken)
        return upsertSocialUser(
            profile = profile.copy(
                firstName = request.firstName.orEmpty().ifBlank { profile.firstName },
                lastName = request.lastName.orEmpty().ifBlank { profile.lastName },
            ),
            method = AuthMethod.APPLE,
        )
    }

    private suspend fun upsertSocialUser(profile: SocialProfile, method: AuthMethod): AuthSession {
        val email = normalizeEmail(profile.email)
        validateEmail(email)
        val existing = users.findByEmail(email)
        if (existing != null) {
            val user = users.addAuthMethod(existing.model.id, method)
            return sessionFor(user)
        }
        val user = users.create(
            NewUser(
                id = UUID.randomUUID().toString(),
                email = email,
                passwordHash = null,
                authMethod = method.name,
                firstName = profile.firstName.trim(),
                lastName = profile.lastName.trim(),
                createdAt = timeProvider.nowMillis(),
            ),
        )
        return sessionFor(user)
    }

    private fun sessionFor(user: UserModel): AuthSession = AuthSession(
        user = user,
        accessToken = tokens.createAccessToken(user.id, authConfig.accessTokenTtlSeconds),
    )

    private fun buildVerificationLink(email: String, code: String): String {
        val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8)
        return "${authConfig.verificationLinkBase}?email=$encodedEmail&code=$code"
    }

    private fun hashVerificationCode(email: String, code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("${authConfig.jwtSecret}:$email:$code".toByteArray(StandardCharsets.UTF_8))
        return HexFormat.of().formatHex(bytes)
    }

    companion object {
        private const val CODE_LENGTH = 6
        private const val MAX_CODE_ATTEMPTS = 5
        private const val MIN_PASSWORD_LENGTH = 8
        private const val INVALID_CODE = "Invalid or expired verification code"
        private const val INVALID_CREDENTIALS = "Invalid email or password"
        private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

        private fun normalizeEmail(email: String): String = email.trim().lowercase()

        private fun validateEmail(email: String) {
            if (!EMAIL_REGEX.matches(email)) {
                throw ValidationException("A valid email is required")
            }
        }

        private fun validateName(value: String, field: String) {
            if (value.isBlank()) {
                throw ValidationException("$field must not be blank")
            }
        }

        private fun validatePassword(password: String) {
            if (password.length < MIN_PASSWORD_LENGTH) {
                throw ValidationException("Password must be at least $MIN_PASSWORD_LENGTH characters")
            }
        }
    }
}
