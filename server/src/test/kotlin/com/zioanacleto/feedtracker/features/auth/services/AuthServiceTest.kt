package com.zioanacleto.feedtracker.features.auth.services

import com.zioanacleto.feedtracker.common.exceptions.ConflictException
import com.zioanacleto.feedtracker.common.exceptions.UnauthorizedException
import com.zioanacleto.feedtracker.common.exceptions.ValidationException
import com.zioanacleto.feedtracker.config.AuthConfig
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.CompleteEmailRegistrationRequest
import com.zioanacleto.feedtracker.domain.auth.EmailLoginRequest
import com.zioanacleto.feedtracker.domain.auth.ResetPasswordRequest
import com.zioanacleto.feedtracker.domain.auth.SocialLoginRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.UpdateProfileRequest
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.DurationDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCode
import com.zioanacleto.feedtracker.features.auth.models.NewUser
import com.zioanacleto.feedtracker.features.auth.models.StoredUser
import com.zioanacleto.feedtracker.features.auth.repositories.EmailVerificationRepository
import com.zioanacleto.feedtracker.features.auth.repositories.RevokedAccessTokenRepository
import com.zioanacleto.feedtracker.features.auth.repositories.UserRepository
import com.zioanacleto.feedtracker.features.trackingpreferences.repositories.TrackingPreferencesRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking

class AuthServiceTest :
    DescribeSpec({

        describe("AuthService") {
            val users = mockk<UserRepository>()
            val verifications = mockk<EmailVerificationRepository>(relaxUnitFun = true)
            val emailSender = mockk<EmailSender>(relaxUnitFun = true)
            val passwordHasher = mockk<PasswordHasher>()
            val tokens = mockk<TokenService>()
            val codes = mockk<VerificationCodeGenerator>()
            val socialVerifier = mockk<SocialTokenVerifier>()
            val revokedTokens = mockk<RevokedAccessTokenRepository>(relaxUnitFun = true)
            val trackingPreferences = mockk<TrackingPreferencesRepository>()
            val timeProvider = TimeProvider { 1_000_000L }
            val authConfig = AuthConfig(
                jwtSecret = "test-secret-that-is-long-enough-32b",
                accessTokenTtlSeconds = 3600,
                registrationTokenTtlSeconds = 900,
                verificationCodeTtlSeconds = 900,
                verificationLinkBase = "feedtracker://auth/verify",
                googleClientId = "google-client",
                appleAudience = "apple-aud",
            )
            val service = AuthServiceImpl(
                users = users,
                verifications = verifications,
                emailSender = emailSender,
                passwordHasher = passwordHasher,
                tokens = tokens,
                codes = codes,
                socialVerifier = socialVerifier,
                timeProvider = timeProvider,
                authConfig = authConfig,
                revokedTokens = revokedTokens,
                trackingPreferences = trackingPreferences,
            )

            it("returns email plus configured social methods") {
                service.availableAuthMethods() shouldBe listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE, AuthMethod.APPLE)
            }

            it("omits social methods that are not configured") {
                val emailOnly = AuthServiceImpl(
                    users = users,
                    verifications = verifications,
                    emailSender = emailSender,
                    passwordHasher = passwordHasher,
                    tokens = tokens,
                    codes = codes,
                    socialVerifier = socialVerifier,
                    timeProvider = timeProvider,
                    authConfig = authConfig.copy(googleClientId = "", appleAudience = ""),
                    revokedTokens = revokedTokens,
                    trackingPreferences = trackingPreferences,
                )

                emailOnly.availableAuthMethods() shouldBe listOf(AuthMethod.EMAIL)
            }

            val user = UserModel(
                id = "user-1",
                email = "mario@example.com",
                authMethods = listOf(AuthMethod.EMAIL),
                firstName = "Mario",
                lastName = "Rossi",
            )

            it("sends a verification code for a new email") {
                coEvery { users.findByEmail("mario@example.com") } returns null
                every { codes.generate() } returns "123456"
                coEvery { verifications.replaceActiveCode(any(), any(), any(), any(), any()) } returns mockk()
                coEvery { emailSender.sendVerification(any(), any(), any()) } returns Unit

                runBlocking { service.startEmailRegistration(StartEmailAuthRequest("Mario@Example.com")) }

                coVerify {
                    emailSender.sendVerification(
                        "mario@example.com",
                        "123456",
                        "feedtracker://auth/verify?email=mario%40example.com&code=123456",
                    )
                }
            }

            it("sends a verification code to add email login on an existing SSO account") {
                val googleUser = user.copy(authMethods = listOf(AuthMethod.GOOGLE))
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(googleUser, null)
                every { codes.generate() } returns "123456"
                coEvery { verifications.replaceActiveCode(any(), any(), any(), any(), any()) } returns mockk()
                coEvery { emailSender.sendVerification(any(), any(), any()) } returns Unit

                runBlocking { service.startEmailRegistration(StartEmailAuthRequest("mario@example.com")) }

                coVerify { emailSender.sendVerification("mario@example.com", "123456", any()) }
            }

            it("rejects starting registration when the email already has a password") {
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(user, "hash")

                shouldThrow<ConflictException> {
                    runBlocking { service.startEmailRegistration(StartEmailAuthRequest("mario@example.com")) }
                }
            }

            it("rejects invalid emails") {
                shouldThrow<ValidationException> {
                    runBlocking { service.startEmailRegistration(StartEmailAuthRequest("not-an-email")) }
                }
            }

            it("returns a registration token when the code matches") {
                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns EmailVerificationCode(
                    id = "code-1",
                    email = "mario@example.com",
                    purpose = "registration",
                    codeHash = "will-be-replaced",
                    expiresAt = 2_000_000L,
                    attemptCount = 0,
                    consumedAt = null,
                    createdAt = 1L,
                )
                every { tokens.createRegistrationToken("mario@example.com", 900) } returns "reg-token"

                val hashSlot = slot<String>()
                coEvery { users.findByEmail(any()) } returns null
                every { codes.generate() } returns "123456"
                coEvery { verifications.replaceActiveCode(any(), capture(hashSlot), any(), any(), any()) } returns mockk()
                coEvery { emailSender.sendVerification(any(), any(), any()) } returns Unit
                runBlocking { service.startEmailRegistration(StartEmailAuthRequest("mario@example.com")) }

                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns EmailVerificationCode(
                    id = "code-1",
                    email = "mario@example.com",
                    purpose = "registration",
                    codeHash = hashSlot.captured,
                    expiresAt = 2_000_000L,
                    attemptCount = 0,
                    consumedAt = null,
                    createdAt = 1L,
                )

                val result = runBlocking {
                    service.verifyEmailCode(VerifyEmailCodeRequest("mario@example.com", "123456"))
                }

                result.registrationToken shouldBe "reg-token"
                coVerify { verifications.consume("code-1", 1_000_000L) }
            }

            it("rejects a wrong verification code") {
                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns EmailVerificationCode(
                    id = "code-1",
                    email = "mario@example.com",
                    purpose = "registration",
                    codeHash = "abc",
                    expiresAt = 2_000_000L,
                    attemptCount = 0,
                    consumedAt = null,
                    createdAt = 1L,
                )

                shouldThrow<UnauthorizedException> {
                    runBlocking { service.verifyEmailCode(VerifyEmailCodeRequest("mario@example.com", "000000")) }
                }
                coVerify { verifications.incrementAttempts("code-1") }
            }

            it("creates an email user after registration is completed") {
                every { tokens.parseRegistrationToken("reg-token") } returns "mario@example.com"
                coEvery { users.findByEmail("mario@example.com") } returns null
                every { passwordHasher.hash("password1") } returns "hashed"
                coEvery { users.create(any()) } returns user
                every { tokens.createAccessToken("user-1", 3600) } returns "access"

                val session = runBlocking {
                    service.completeEmailRegistration(
                        CompleteEmailRegistrationRequest(
                            registrationToken = "reg-token",
                            password = "password1",
                            firstName = "Mario",
                            lastName = "Rossi",
                        ),
                    )
                }

                session.accessToken shouldBe "access"
                session.user shouldBe user
                val created = slot<NewUser>()
                coVerify { users.create(capture(created)) }
                created.captured.passwordHash shouldBe "hashed"
                created.captured.authMethod shouldBe AuthMethod.EMAIL.name
            }

            it("adds a password to an existing SSO account after email verification") {
                every { tokens.parseRegistrationToken("reg-token") } returns "mario@example.com"
                val googleUser = user.copy(authMethods = listOf(AuthMethod.GOOGLE))
                val linked = user.copy(authMethods = listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE))
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(googleUser, null)
                every { passwordHasher.hash("password1") } returns "hashed"
                coEvery { users.addAuthMethod("user-1", AuthMethod.EMAIL, "hashed") } returns linked
                every { tokens.createAccessToken("user-1", 3600) } returns "access"

                val session = runBlocking {
                    service.completeEmailRegistration(
                        CompleteEmailRegistrationRequest(
                            registrationToken = "reg-token",
                            password = "password1",
                            firstName = "Mario",
                            lastName = "Rossi",
                        ),
                    )
                }

                session.user shouldBe linked
                coVerify { users.addAuthMethod("user-1", AuthMethod.EMAIL, "hashed") }
            }

            it("logs in an email user with a valid password") {
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(user, "hashed")
                every { passwordHasher.verify("password1", "hashed") } returns true
                every { tokens.createAccessToken("user-1", 3600) } returns "access"

                val session = runBlocking {
                    service.loginWithEmail(EmailLoginRequest("mario@example.com", "password1"))
                }

                session.accessToken shouldBe "access"
            }

            it("rejects login with the wrong password") {
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(user, "hashed")
                every { passwordHasher.verify("nope", "hashed") } returns false

                shouldThrow<UnauthorizedException> {
                    runBlocking { service.loginWithEmail(EmailLoginRequest("mario@example.com", "nope")) }
                }
            }

            it("creates a Google user on first SSO") {
                coEvery { socialVerifier.verifyGoogle("id-token") } returns SocialProfile(
                    email = "mario@example.com",
                    firstName = "Mario",
                    lastName = "Rossi",
                )
                coEvery { users.findByEmail("mario@example.com") } returns null
                val googleUser = user.copy(authMethods = listOf(AuthMethod.GOOGLE))
                coEvery { users.create(any()) } returns googleUser
                every { tokens.createAccessToken("user-1", 3600) } returns "access"

                val session = runBlocking { service.loginWithGoogle(SocialLoginRequest("id-token")) }

                session.user.authMethods shouldBe listOf(AuthMethod.GOOGLE)
                session.accessToken shouldBe "access"
            }

            it("logs in an existing Apple user and uses client-provided names on first profile") {
                coEvery { socialVerifier.verifyApple("id-token") } returns SocialProfile(
                    email = "mario@example.com",
                    firstName = "",
                    lastName = "",
                )
                val appleUser = user.copy(authMethods = listOf(AuthMethod.APPLE))
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(appleUser, null)
                coEvery { users.addAuthMethod("user-1", AuthMethod.APPLE, null) } returns appleUser
                every { tokens.createAccessToken("user-1", 3600) } returns "access"

                val session = runBlocking {
                    service.loginWithApple(
                        SocialLoginRequest("id-token", firstName = "Mario", lastName = "Rossi"),
                    )
                }

                session.user shouldBe appleUser
            }

            it("links Google to an existing email account when the addresses match") {
                coEvery { socialVerifier.verifyGoogle("id-token") } returns SocialProfile(
                    email = "mario@example.com",
                    firstName = "Mario",
                    lastName = "Rossi",
                )
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(user, "hashed")
                val linked = user.copy(authMethods = listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE))
                coEvery { users.addAuthMethod("user-1", AuthMethod.GOOGLE, null) } returns linked
                every { tokens.createAccessToken("user-1", 3600) } returns "access"

                val session = runBlocking { service.loginWithGoogle(SocialLoginRequest("id-token")) }

                session.user.id shouldBe "user-1"
                session.user.authMethods shouldBe listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE)
                coVerify { users.addAuthMethod("user-1", AuthMethod.GOOGLE, null) }
            }

            it("updates first and last name for an authenticated user") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
                    issuedAtMillis = 1_000_000L,
                )
                coEvery { revokedTokens.isRevoked("jti-1") } returns false
                coEvery { users.findStoredById("user-1") } returns StoredUser(user, "hashed")
                coEvery { users.findById("user-1") } returns user
                val updated = user.copy(firstName = "Luigi", lastName = "Bianchi")
                coEvery { users.updateNames("user-1", "Luigi", "Bianchi") } returns updated

                val result = runBlocking {
                    service.updateProfile("access", UpdateProfileRequest(" Luigi ", "Bianchi"))
                }

                result shouldBe updated
            }

            it("returns default tracking preferences when none are stored") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
                    issuedAtMillis = 1_000_000L,
                )
                coEvery { revokedTokens.isRevoked("jti-1") } returns false
                coEvery { users.findStoredById("user-1") } returns StoredUser(user, "hashed")
                coEvery { trackingPreferences.findByUserId("user-1") } returns null

                val result = runBlocking { service.getTrackingPreferences("access") }

                result shouldBe TrackingPreferences.Default
            }

            it("saves sanitized tracking preferences for an authenticated user") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
                    issuedAtMillis = 1_000_000L,
                )
                coEvery { revokedTokens.isRevoked("jti-1") } returns false
                coEvery { users.findStoredById("user-1") } returns StoredUser(user, "hashed")
                val request = TrackingPreferences(
                    dateFormat = DateDisplayFormat.MONTH_DAY_YEAR,
                    dayStartHour = 6,
                    dayStartMinute = 30,
                    durationFormat = DurationDisplayFormat.HOURS_MINUTES,
                    defaultPersonName = " Luigi ",
                    defaultPersonSurname = " Bianchi ",
                    defaultPersonBirthDate = "02/02/1991",
                )
                val saved = slot<TrackingPreferences>()
                coEvery { trackingPreferences.upsert("user-1", capture(saved)) } answers { saved.captured }

                val result = runBlocking { service.updateTrackingPreferences("access", request) }

                result.defaultPersonName shouldBe "Luigi"
                result.dayStartHour shouldBe 6
                saved.captured.defaultPersonSurname shouldBe "Bianchi"
            }

            it("rejects an invalid day start hour") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
                    issuedAtMillis = 1_000_000L,
                )
                coEvery { revokedTokens.isRevoked("jti-1") } returns false
                coEvery { users.findStoredById("user-1") } returns StoredUser(user, "hashed")

                shouldThrow<ValidationException> {
                    runBlocking {
                        service.updateTrackingPreferences(
                            "access",
                            TrackingPreferences.Default.copy(dayStartHour = 24),
                        )
                    }
                }
            }

            it("rejects a blank first name when updating the profile") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
                    issuedAtMillis = 1_000_000L,
                )
                coEvery { revokedTokens.isRevoked("jti-1") } returns false
                coEvery { users.findStoredById("user-1") } returns StoredUser(user, "hashed")

                shouldThrow<ValidationException> {
                    runBlocking { service.updateProfile("access", UpdateProfileRequest("  ", "Bianchi")) }
                }
            }

            it("rejects a revoked access token when updating the profile") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
                    issuedAtMillis = 1_000_000L,
                )
                coEvery { revokedTokens.isRevoked("jti-1") } returns true

                shouldThrow<UnauthorizedException> {
                    runBlocking { service.updateProfile("access", UpdateProfileRequest("Luigi", "Bianchi")) }
                }
            }

            it("revokes a valid access token on logout") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
                    issuedAtMillis = 1_000_000L,
                )

                runBlocking { service.logout("access") }

                coVerify { revokedTokens.revoke("jti-1", 2_000_000L, 1_000_000L) }
            }

            it("rejects logout with an invalid access token") {
                every { tokens.parseAccessToken("bad") } throws UnauthorizedException("Invalid access token")

                shouldThrow<UnauthorizedException> {
                    runBlocking { service.logout("bad") }
                }
            }

            it("sends a password reset code for an email account without revealing existence") {
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(user, "hashed")
                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns null
                every { codes.generate() } returns "123456"
                coEvery { verifications.replaceActiveCode(any(), any(), any(), any(), any()) } returns mockk()
                coEvery { emailSender.sendPasswordReset(any(), any(), any(), any()) } returns Unit

                runBlocking { service.startPasswordReset(StartEmailAuthRequest("Mario@Example.com")) }

                coVerify {
                    emailSender.sendPasswordReset(
                        "mario@example.com",
                        "123456",
                        "feedtracker://auth/verify?email=mario%40example.com&code=123456&purpose=reset",
                        false,
                    )
                }
            }

            it("does not send a reset email when the address is unknown") {
                coEvery { users.findByEmail("ghost@example.com") } returns null

                runBlocking { service.startPasswordReset(StartEmailAuthRequest("ghost@example.com")) }

                coVerify(exactly = 0) { emailSender.sendPasswordReset("ghost@example.com", any(), any(), any()) }
                coVerify(exactly = 0) { verifications.replaceActiveCode("ghost@example.com", any(), any(), any(), any()) }
            }

            it("invites SSO-only accounts to set a password") {
                val googleUser = user.copy(authMethods = listOf(AuthMethod.GOOGLE))
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(googleUser, null)
                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns null
                every { codes.generate() } returns "123456"
                coEvery { verifications.replaceActiveCode(any(), any(), any(), any(), any()) } returns mockk()
                coEvery { emailSender.sendPasswordReset(any(), any(), any(), any()) } returns Unit

                runBlocking { service.startPasswordReset(StartEmailAuthRequest("mario@example.com")) }

                coVerify { emailSender.sendPasswordReset("mario@example.com", "123456", any(), true) }
            }

            it("rate-limits password reset emails") {
                coEvery { users.findByEmail("limited@example.com") } returns StoredUser(user, "hashed")
                coEvery { verifications.findActiveByEmail("limited@example.com", any()) } returns EmailVerificationCode(
                    id = "code-1",
                    email = "limited@example.com",
                    purpose = "password_reset",
                    codeHash = "hash",
                    expiresAt = 2_000_000L,
                    attemptCount = 0,
                    consumedAt = null,
                    createdAt = 980_000L,
                )

                runBlocking { service.startPasswordReset(StartEmailAuthRequest("limited@example.com")) }

                coVerify(exactly = 0) { emailSender.sendPasswordReset("limited@example.com", any(), any(), any()) }
            }

            it("returns a reset token when the reset code matches") {
                val hashSlot = slot<String>()
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(user, "hashed")
                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns null
                every { codes.generate() } returns "123456"
                coEvery { verifications.replaceActiveCode(any(), capture(hashSlot), any(), any(), any()) } returns mockk()
                coEvery { emailSender.sendPasswordReset(any(), any(), any(), any()) } returns Unit
                every { tokens.createPasswordResetToken("mario@example.com", 900) } returns "reset-token"
                runBlocking { service.startPasswordReset(StartEmailAuthRequest("mario@example.com")) }

                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns EmailVerificationCode(
                    id = "code-1",
                    email = "mario@example.com",
                    purpose = "password_reset",
                    codeHash = hashSlot.captured,
                    expiresAt = 2_000_000L,
                    attemptCount = 0,
                    consumedAt = null,
                    createdAt = 1L,
                )

                val result = runBlocking {
                    service.verifyPasswordResetCode(VerifyEmailCodeRequest("mario@example.com", "123456"))
                }

                result.resetToken shouldBe "reset-token"
            }

            it("updates the password and invalidates previous sessions") {
                every { tokens.parsePasswordResetToken("reset-token") } returns "mario@example.com"
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(user, "old-hash")
                every { passwordHasher.hash("password2") } returns "new-hash"
                coEvery { users.updatePassword("user-1", "new-hash", 1_000_000L) } returns user
                every { tokens.createAccessToken("user-1", 3600) } returns "new-access"

                val session = runBlocking {
                    service.resetPassword(ResetPasswordRequest("reset-token", "password2"))
                }

                session.accessToken shouldBe "new-access"
                coVerify { users.updatePassword("user-1", "new-hash", 1_000_000L) }
            }

            it("does not fail the request when the reset email cannot be sent") {
                coEvery { users.findByEmail("mario@example.com") } returns StoredUser(user, "hashed")
                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns null
                every { codes.generate() } returns "123456"
                coEvery { verifications.replaceActiveCode(any(), any(), any(), any(), any()) } returns mockk()
                coEvery { emailSender.sendPasswordReset(any(), any(), any(), any()) } throws RuntimeException("smtp down")

                runBlocking { service.startPasswordReset(StartEmailAuthRequest("mario@example.com")) }
            }

            it("increments attempts when the reset code does not match") {
                coEvery { verifications.findActiveByEmail("mario@example.com", any()) } returns EmailVerificationCode(
                    id = "code-1",
                    email = "mario@example.com",
                    purpose = "password_reset",
                    codeHash = "abc",
                    expiresAt = 2_000_000L,
                    attemptCount = 0,
                    consumedAt = null,
                    createdAt = 1L,
                )

                shouldThrow<UnauthorizedException> {
                    runBlocking { service.verifyPasswordResetCode(VerifyEmailCodeRequest("mario@example.com", "000000")) }
                }
                coVerify { verifications.incrementAttempts("code-1") }
            }

            it("rejects a reset token for an unknown email") {
                every { tokens.parsePasswordResetToken("reset-token") } returns "ghost@example.com"
                coEvery { users.findByEmail("ghost@example.com") } returns null

                shouldThrow<UnauthorizedException> {
                    runBlocking { service.resetPassword(ResetPasswordRequest("reset-token", "password2")) }
                }
            }

            it("rejects a short password during reset") {
                every { tokens.parsePasswordResetToken("reset-token") } returns "mario@example.com"

                shouldThrow<ValidationException> {
                    runBlocking { service.resetPassword(ResetPasswordRequest("reset-token", "short")) }
                }
            }

            it("rejects an access token issued before a password reset") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
                    issuedAtMillis = 900_000L,
                )
                coEvery { revokedTokens.isRevoked("jti-1") } returns false
                coEvery { users.findStoredById("user-1") } returns StoredUser(user, "hashed", tokensValidAfter = 1_000_000L)

                shouldThrow<UnauthorizedException> {
                    runBlocking { service.updateProfile("access", UpdateProfileRequest("Luigi", "Bianchi")) }
                }
            }
        }
    })
