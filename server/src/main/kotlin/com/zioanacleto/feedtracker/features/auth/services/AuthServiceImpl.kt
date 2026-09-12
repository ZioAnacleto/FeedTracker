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
import com.zioanacleto.feedtracker.domain.auth.ResetPasswordRequest
import com.zioanacleto.feedtracker.domain.auth.SocialLoginRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.UpdateProfileRequest
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeResponse
import com.zioanacleto.feedtracker.domain.auth.VerifyPasswordResetResponse
import com.zioanacleto.feedtracker.domain.preferences.PersonPrefillMode
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationPurpose
import com.zioanacleto.feedtracker.features.auth.models.NewUser
import com.zioanacleto.feedtracker.features.auth.repositories.EmailVerificationRepository
import com.zioanacleto.feedtracker.features.auth.repositories.RevokedAccessTokenRepository
import com.zioanacleto.feedtracker.features.auth.repositories.UserRepository
import com.zioanacleto.feedtracker.features.trackingpreferences.repositories.TrackingPreferencesRepository
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
    private val trackingPreferences: TrackingPreferencesRepository,
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
            codeHash = hashVerificationCode(email, code, EmailVerificationPurpose.REGISTRATION),
            expiresAt = now + authConfig.verificationCodeTtlSeconds * 1000,
            createdAt = now,
            purpose = EmailVerificationPurpose.REGISTRATION,
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
        val email = consumeMatchingCode(request, EmailVerificationPurpose.REGISTRATION)
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

    override suspend fun startPasswordReset(request: StartEmailAuthRequest) {
        val email = normalizeEmail(request.email)
        validateEmail(email)
        val stored = users.findByEmail(email) ?: return
        val now = timeProvider.nowMillis()
        val active = verifications.findActiveByEmail(email, EmailVerificationPurpose.PASSWORD_RESET)
        if (active != null && now - active.createdAt < RESET_RESEND_COOLDOWN_MS) {
            return
        }
        val code = codes.generate()
        verifications.replaceActiveCode(
            email = email,
            codeHash = hashVerificationCode(email, code, EmailVerificationPurpose.PASSWORD_RESET),
            expiresAt = now + authConfig.verificationCodeTtlSeconds * 1000,
            createdAt = now,
            purpose = EmailVerificationPurpose.PASSWORD_RESET,
        )
        val link = buildPasswordResetLink(email, code)
        val ssoOnly = stored.passwordHash == null
        runCatching { emailSender.sendPasswordReset(email, code, link, ssoOnly) }
    }

    override suspend fun verifyPasswordResetCode(request: VerifyEmailCodeRequest): VerifyPasswordResetResponse {
        val email = consumeMatchingCode(request, EmailVerificationPurpose.PASSWORD_RESET)
        return VerifyPasswordResetResponse(
            resetToken = tokens.createPasswordResetToken(email, authConfig.registrationTokenTtlSeconds),
        )
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): AuthSession {
        val email = tokens.parsePasswordResetToken(request.resetToken)
        validatePassword(request.password)
        val stored = users.findByEmail(email) ?: throw UnauthorizedException(INVALID_RESET)
        val now = timeProvider.nowMillis()
        val user = users.updatePassword(
            userId = stored.model.id,
            passwordHash = passwordHasher.hash(request.password),
            tokensValidAfter = now,
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

    override suspend fun updateProfile(accessToken: String, request: UpdateProfileRequest): UserModel {
        val claims = requireValidAccessToken(accessToken)
        validateName(request.firstName, "firstName")
        validateName(request.lastName, "lastName")
        users.findById(claims.userId) ?: throw UnauthorizedException("Invalid access token")
        return users.updateNames(claims.userId, request.firstName.trim(), request.lastName.trim())
    }

    override suspend fun getTrackingPreferences(accessToken: String): TrackingPreferences {
        val claims = requireValidAccessToken(accessToken)
        return trackingPreferences.findByUserId(claims.userId) ?: TrackingPreferences.Default
    }

    override suspend fun updateTrackingPreferences(accessToken: String, preferences: TrackingPreferences): TrackingPreferences {
        val claims = requireValidAccessToken(accessToken)
        val sanitized = AuthServiceImpl.sanitizeTrackingPreferences(preferences)
        return trackingPreferences.upsert(claims.userId, sanitized)
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

    private suspend fun requireValidAccessToken(accessToken: String): AccessTokenClaims {
        val claims = tokens.parseAccessToken(accessToken)
        if (revokedTokens.isRevoked(claims.jti)) {
            throw UnauthorizedException("Invalid access token")
        }
        val stored = users.findStoredById(claims.userId) ?: throw UnauthorizedException("Invalid access token")
        if (claims.issuedAtMillis < stored.tokensValidAfter) {
            throw UnauthorizedException("Invalid access token")
        }
        return claims
    }

    private suspend fun consumeMatchingCode(request: VerifyEmailCodeRequest, purpose: String): String {
        val email = normalizeEmail(request.email)
        validateEmail(email)
        val code = request.code.trim()
        if (code.length != CODE_LENGTH || code.any { !it.isDigit() }) {
            throw ValidationException("Verification code must be 6 digits")
        }
        val active = verifications.findActiveByEmail(email, purpose)
            ?: throw UnauthorizedException(INVALID_CODE)
        val now = timeProvider.nowMillis()
        if (active.expiresAt <= now || active.attemptCount >= MAX_CODE_ATTEMPTS) {
            throw UnauthorizedException(INVALID_CODE)
        }
        val expected = hashVerificationCode(email, code, purpose).toByteArray(StandardCharsets.UTF_8)
        val actual = active.codeHash.toByteArray(StandardCharsets.UTF_8)
        if (!MessageDigest.isEqual(expected, actual)) {
            verifications.incrementAttempts(active.id)
            throw UnauthorizedException(INVALID_CODE)
        }
        verifications.consume(active.id, now)
        return email
    }

    private fun sessionFor(user: UserModel): AuthSession = AuthSession(
        user = user,
        accessToken = tokens.createAccessToken(user.id, authConfig.accessTokenTtlSeconds),
    )

    private fun buildVerificationLink(email: String, code: String): String {
        val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8)
        return "${authConfig.verificationLinkBase}?email=$encodedEmail&code=$code"
    }

    private fun buildPasswordResetLink(email: String, code: String): String {
        val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8)
        return "${authConfig.verificationLinkBase}?email=$encodedEmail&code=$code&purpose=reset"
    }

    private fun hashVerificationCode(email: String, code: String, purpose: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("${authConfig.jwtSecret}:$purpose:$email:$code".toByteArray(StandardCharsets.UTF_8))
        return HexFormat.of().formatHex(bytes)
    }

    companion object {
        private const val CODE_LENGTH = 6
        private const val MAX_CODE_ATTEMPTS = 5
        private const val MIN_PASSWORD_LENGTH = 8
        private const val INVALID_CODE = "Invalid or expired verification code"
        private const val INVALID_CREDENTIALS = "Invalid email or password"
        private const val INVALID_RESET = "Invalid or expired reset token"
        private const val RESET_RESEND_COOLDOWN_MS = 60_000L
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

        private val BIRTH_DATE_REGEX = Regex(
            """^(0[1-9]|[12][0-9]|3[01])\/(0[1-9]|1[0-2])\/(19\d{2}|20\d{2})$""",
        )

        fun sanitizeTrackingPreferences(preferences: TrackingPreferences): TrackingPreferences {
            if (preferences.dayStartHour !in 0..23 || preferences.dayStartMinute !in 0..59) {
                throw ValidationException("day start must be a valid time")
            }
            return preferences.copy(
                defaultPersonName = preferences.defaultPersonName.trim(),
                defaultPersonSurname = preferences.defaultPersonSurname.trim(),
                defaultPersonBirthDate = validateOptionalBirthDate(preferences.defaultPersonBirthDate),
                lastUsedPersonName = preferences.lastUsedPersonName.trim(),
                lastUsedPersonSurname = preferences.lastUsedPersonSurname.trim(),
                lastUsedPersonBirthDate = validateOptionalBirthDate(preferences.lastUsedPersonBirthDate),
            ).also { sanitized ->
                if (sanitized.personPrefillMode == PersonPrefillMode.CUSTOM &&
                    sanitized.defaultPersonBirthDate.isNotEmpty() &&
                    (sanitized.defaultPersonName.isEmpty() || sanitized.defaultPersonSurname.isEmpty())
                ) {
                    throw ValidationException("default person name and surname are required when a birth date is set")
                }
            }
        }

        private fun validateOptionalBirthDate(value: String): String {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return ""
            if (!BIRTH_DATE_REGEX.matches(trimmed)) {
                throw ValidationException("birthDate must be in DD/MM/YYYY format")
            }
            return trimmed
        }
    }
}
