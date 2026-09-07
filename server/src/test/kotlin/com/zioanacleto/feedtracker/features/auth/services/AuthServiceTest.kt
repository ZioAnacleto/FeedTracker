package com.zioanacleto.feedtracker.features.auth.services

import com.zioanacleto.feedtracker.common.exceptions.ConflictException
import com.zioanacleto.feedtracker.common.exceptions.UnauthorizedException
import com.zioanacleto.feedtracker.common.exceptions.ValidationException
import com.zioanacleto.feedtracker.config.AuthConfig
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.CompleteEmailRegistrationRequest
import com.zioanacleto.feedtracker.domain.auth.EmailLoginRequest
import com.zioanacleto.feedtracker.domain.auth.SocialLoginRequest
import com.zioanacleto.feedtracker.domain.auth.StartEmailAuthRequest
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeRequest
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCode
import com.zioanacleto.feedtracker.features.auth.models.NewUser
import com.zioanacleto.feedtracker.features.auth.models.StoredUser
import com.zioanacleto.feedtracker.features.auth.repositories.EmailVerificationRepository
import com.zioanacleto.feedtracker.features.auth.repositories.RevokedAccessTokenRepository
import com.zioanacleto.feedtracker.features.auth.repositories.UserRepository
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
                coEvery { verifications.replaceActiveCode(any(), any(), any(), any()) } returns mockk()
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
                coEvery { verifications.replaceActiveCode(any(), any(), any(), any()) } returns mockk()
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
                coEvery { verifications.findActiveByEmail("mario@example.com") } returns EmailVerificationCode(
                    id = "code-1",
                    email = "mario@example.com",
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
                coEvery { verifications.replaceActiveCode(any(), capture(hashSlot), any(), any()) } returns mockk()
                coEvery { emailSender.sendVerification(any(), any(), any()) } returns Unit
                runBlocking { service.startEmailRegistration(StartEmailAuthRequest("mario@example.com")) }

                coEvery { verifications.findActiveByEmail("mario@example.com") } returns EmailVerificationCode(
                    id = "code-1",
                    email = "mario@example.com",
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
                coEvery { verifications.findActiveByEmail("mario@example.com") } returns EmailVerificationCode(
                    id = "code-1",
                    email = "mario@example.com",
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

            it("revokes a valid access token on logout") {
                every { tokens.parseAccessToken("access") } returns AccessTokenClaims(
                    userId = "user-1",
                    jti = "jti-1",
                    expiresAtMillis = 2_000_000L,
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
        }
    })
