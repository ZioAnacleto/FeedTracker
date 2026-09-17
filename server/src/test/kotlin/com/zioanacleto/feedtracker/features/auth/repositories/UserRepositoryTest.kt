package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.config.DatabaseConfig
import com.zioanacleto.feedtracker.config.DatabaseFactory
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCodesTable
import com.zioanacleto.feedtracker.features.auth.models.NewUser
import com.zioanacleto.feedtracker.features.auth.models.UserAuthMethodsTable
import com.zioanacleto.feedtracker.features.auth.models.UsersTable
import com.zioanacleto.feedtracker.features.trackingpreferences.models.TrackingPreferencesTable
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryTest :
    DescribeSpec({

        val repository = UserRepositoryImpl()
        val verificationRepository = EmailVerificationRepositoryImpl()

        beforeSpec {
            DatabaseFactory.init(
                DatabaseConfig(
                    driver = "org.h2.Driver",
                    url = "jdbc:h2:mem:feedtracker-auth;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                    user = "sa",
                    password = "",
                    maxPoolSize = 5,
                ),
            )
        }

        beforeEach {
            transaction {
                EmailVerificationCodesTable.deleteAll()
                UserAuthMethodsTable.deleteAll()
                TrackingPreferencesTable.deleteAll()
                UsersTable.deleteAll()
            }
        }

        describe("UserRepository") {
            it("creates and finds a user by email") {
                val created = runBlocking {
                    repository.create(
                        NewUser(
                            id = "user-1",
                            email = "mario@example.com",
                            passwordHash = "hashed",
                            authMethod = AuthMethod.EMAIL.name,
                            firstName = "Mario",
                            lastName = "Rossi",
                            createdAt = 1_000L,
                        ),
                    )
                }

                val found = runBlocking { repository.findByEmail("mario@example.com") }

                created.email shouldBe "mario@example.com"
                found.shouldNotBeNull()
                found.passwordHash shouldBe "hashed"
                found.model.firstName shouldBe "Mario"
                found.model.authMethods shouldBe listOf(AuthMethod.EMAIL)
            }

            it("links a second provider to the same user") {
                runBlocking {
                    repository.create(
                        NewUser(
                            id = "user-1",
                            email = "mario@example.com",
                            passwordHash = "hashed",
                            authMethod = AuthMethod.EMAIL.name,
                            firstName = "Mario",
                            lastName = "Rossi",
                            createdAt = 1_000L,
                        ),
                    )
                }

                val linked = runBlocking { repository.addAuthMethod("user-1", AuthMethod.GOOGLE) }
                val found = runBlocking { repository.findByEmail("mario@example.com") }

                linked.authMethods shouldBe listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE)
                found?.model?.authMethods shouldBe listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE)
                found?.passwordHash shouldBe "hashed"
            }

            it("returns null when the email is unknown") {
                val found = runBlocking { repository.findByEmail("missing@example.com") }
                found.shouldBeNull()
            }

            it("updates first and last name") {
                runBlocking {
                    repository.create(
                        NewUser(
                            id = "user-1",
                            email = "mario@example.com",
                            passwordHash = "hashed",
                            authMethod = AuthMethod.EMAIL.name,
                            firstName = "Mario",
                            lastName = "Rossi",
                            createdAt = 1_000L,
                        ),
                    )
                }

                val updated = runBlocking { repository.updateNames("user-1", "Luigi", "Bianchi") }
                val found = runBlocking { repository.findById("user-1") }

                updated.firstName shouldBe "Luigi"
                updated.lastName shouldBe "Bianchi"
                found?.firstName shouldBe "Luigi"
                found?.lastName shouldBe "Bianchi"
                found?.email shouldBe "mario@example.com"
            }

            it("updates the password and links email login") {
                runBlocking {
                    repository.create(
                        NewUser(
                            id = "user-1",
                            email = "mario@example.com",
                            passwordHash = null,
                            authMethod = AuthMethod.GOOGLE.name,
                            firstName = "Mario",
                            lastName = "Rossi",
                            createdAt = 1_000L,
                        ),
                    )
                }

                val updated = runBlocking { repository.updatePassword("user-1", "new-hash", 2_000L) }
                val found = runBlocking { repository.findByEmail("mario@example.com") }

                updated.authMethods shouldBe listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE)
                found?.passwordHash shouldBe "new-hash"
                found?.tokensValidAfter shouldBe 2_000L
            }
        }

        describe("EmailVerificationRepository") {
            it("replaces the active code for an email") {
                runBlocking {
                    verificationRepository.replaceActiveCode("mario@example.com", "hash-1", 2_000L, 1_000L)
                    verificationRepository.replaceActiveCode("mario@example.com", "hash-2", 3_000L, 1_500L)
                }

                val active = runBlocking { verificationRepository.findActiveByEmail("mario@example.com") }

                active.shouldNotBeNull()
                active.codeHash shouldBe "hash-2"
            }

            it("consumes a code so it is no longer active") {
                val created = runBlocking {
                    verificationRepository.replaceActiveCode("mario@example.com", "hash-1", 2_000L, 1_000L)
                }

                runBlocking { verificationRepository.consume(created.id, 1_800L) }
                val active = runBlocking { verificationRepository.findActiveByEmail("mario@example.com") }

                active.shouldBeNull()
            }

            it("increments attempt count on an active code") {
                val created = runBlocking {
                    verificationRepository.replaceActiveCode("mario@example.com", "hash-1", 2_000L, 1_000L)
                }

                runBlocking { verificationRepository.incrementAttempts(created.id) }
                val active = runBlocking { verificationRepository.findActiveByEmail("mario@example.com") }

                active.shouldNotBeNull()
                active.attemptCount shouldBe 1
            }

            it("keeps registration and password reset codes independent") {
                runBlocking {
                    verificationRepository.replaceActiveCode(
                        email = "mario@example.com",
                        codeHash = "reg-hash",
                        expiresAt = 2_000L,
                        createdAt = 1_000L,
                    )
                    verificationRepository.replaceActiveCode(
                        email = "mario@example.com",
                        codeHash = "reset-hash",
                        expiresAt = 2_000L,
                        createdAt = 1_000L,
                        purpose = "password_reset",
                    )
                }

                val registration = runBlocking { verificationRepository.findActiveByEmail("mario@example.com") }
                val reset = runBlocking {
                    verificationRepository.findActiveByEmail("mario@example.com", "password_reset")
                }

                registration?.codeHash shouldBe "reg-hash"
                reset?.codeHash shouldBe "reset-hash"
            }
        }
    })
