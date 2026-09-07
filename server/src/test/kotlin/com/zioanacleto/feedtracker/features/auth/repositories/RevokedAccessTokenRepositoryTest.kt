package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.config.DatabaseConfig
import com.zioanacleto.feedtracker.config.DatabaseFactory
import com.zioanacleto.feedtracker.features.auth.models.RevokedAccessTokensTable
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class RevokedAccessTokenRepositoryTest :
    DescribeSpec({

        val repository = RevokedAccessTokenRepositoryImpl()

        beforeSpec {
            DatabaseFactory.init(
                DatabaseConfig(
                    driver = "org.h2.Driver",
                    url = "jdbc:h2:mem:feedtracker-revoked-tokens;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                    user = "sa",
                    password = "",
                    maxPoolSize = 5,
                ),
            )
        }

        beforeEach {
            transaction { RevokedAccessTokensTable.deleteAll() }
        }

        describe("RevokedAccessTokenRepository") {
            it("revokes a token id and reports it as revoked") {
                runBlocking { repository.revoke("jti-1", expiresAt = 2_000L, revokedAt = 1_000L) }

                runBlocking { repository.isRevoked("jti-1") } shouldBe true
                runBlocking { repository.isRevoked("jti-2") } shouldBe false
            }

            it("is idempotent when the same token is revoked twice") {
                runBlocking {
                    repository.revoke("jti-1", expiresAt = 2_000L, revokedAt = 1_000L)
                    repository.revoke("jti-1", expiresAt = 2_000L, revokedAt = 1_500L)
                }

                runBlocking { repository.isRevoked("jti-1") } shouldBe true
            }

            it("drops expired revocations when a new token is revoked") {
                runBlocking {
                    repository.revoke("expired", expiresAt = 1_000L, revokedAt = 500L)
                    repository.revoke("active", expiresAt = 3_000L, revokedAt = 1_000L)
                }

                runBlocking { repository.isRevoked("expired") } shouldBe false
                runBlocking { repository.isRevoked("active") } shouldBe true
            }
        }
    })
