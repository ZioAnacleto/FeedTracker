package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.config.DatabaseConfig
import com.zioanacleto.feedtracker.config.DatabaseFactory
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCodesTable
import com.zioanacleto.feedtracker.features.auth.models.NewUser
import com.zioanacleto.feedtracker.features.auth.models.UserAuthMethodsTable
import com.zioanacleto.feedtracker.features.auth.models.UsersTable
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
        }
    })
