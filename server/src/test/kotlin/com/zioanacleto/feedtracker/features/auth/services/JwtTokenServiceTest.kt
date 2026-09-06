package com.zioanacleto.feedtracker.features.auth.services

import com.zioanacleto.feedtracker.common.exceptions.UnauthorizedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

class JwtTokenServiceTest :
    FunSpec({

        val timeProvider = TimeProvider { 1_700_000_000_000L }
        val service = JwtTokenService("feedtracker-test-jwt-secret-32bytes!", timeProvider)

        test("round-trips a registration token") {
            val token = service.createRegistrationToken("mario@example.com", 900)

            service.parseRegistrationToken(token) shouldBe "mario@example.com"
        }

        test("creates a non-blank access token") {
            service.createAccessToken("user-1", 3600).shouldNotBeBlank()
        }

        test("rejects an expired registration token") {
            val token = service.createRegistrationToken("mario@example.com", 1)
            val expiredService = JwtTokenService(
                "feedtracker-test-jwt-secret-32bytes!",
                TimeProvider { 1_700_000_000_000L + 2_000 },
            )

            shouldThrow<UnauthorizedException> {
                expiredService.parseRegistrationToken(token)
            }
        }

        test("rejects an access token used as registration token") {
            val token = service.createAccessToken("user-1", 3600)

            shouldThrow<UnauthorizedException> {
                service.parseRegistrationToken(token)
            }
        }
    })

class BcryptPasswordHasherTest :
    FunSpec({

        val hasher = BcryptPasswordHasher()

        test("hashes and verifies a password") {
            val hash = hasher.hash("password1")

            hasher.verify("password1", hash) shouldBe true
            hasher.verify("other", hash) shouldBe false
        }
    })
