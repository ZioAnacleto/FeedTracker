package com.zioanacleto.feedtracker.features.trackingsessions.repositories

import com.zioanacleto.feedtracker.config.DatabaseConfig
import com.zioanacleto.feedtracker.config.DatabaseFactory
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest
import com.zioanacleto.feedtracker.features.trackingsessions.models.TrackingSessionsTable
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class TrackingSessionRepositoryTest :
    DescribeSpec({

        val repository = TrackingSessionRepositoryImpl()

        beforeSpec {
            DatabaseFactory.init(
                DatabaseConfig(
                    driver = "org.h2.Driver",
                    url = "jdbc:h2:mem:feedtracker;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                    user = "sa",
                    password = "",
                    maxPoolSize = 5,
                ),
            )
        }

        beforeEach {
            transaction {
                TrackingSessionsTable.deleteAll()
            }
        }

        describe("TrackingSessionRepository") {
            val createRequest = CreateTrackingSessionRequest(
                sessionStartTime = 1000L,
                sessionEndTime = 2000L,
                name = "Mario",
                surname = "Rossi",
                birthDate = "01/01/1990",
                additionalNotes = "note",
            )

            it("creates and retrieves a session") {
                val created = runBlocking { repository.create("user-1", createRequest) }
                val found = runBlocking { repository.findById("user-1", created.id) }

                found shouldBe created
            }

            it("lists only the owner's sessions") {
                runBlocking { repository.create("user-1", createRequest) }
                runBlocking { repository.create("user-1", createRequest.copy(name = "Luigi")) }
                runBlocking { repository.create("user-2", createRequest.copy(name = "Anna")) }

                val all = runBlocking { repository.findAll("user-1") }

                all shouldHaveSize 2
                all.map { it.name }.toSet() shouldBe setOf("Mario", "Luigi")
            }

            it("updates a session") {
                val created = runBlocking { repository.create("user-1", createRequest) }
                val updateRequest = UpdateTrackingSessionRequest(
                    sessionStartTime = 1000L,
                    sessionEndTime = 4000L,
                    name = "Mario",
                    surname = "Rossi",
                    birthDate = "01/01/1990",
                    additionalNotes = null,
                )

                val updated = runBlocking { repository.update("user-1", created.id, updateRequest) }
                val found = runBlocking { repository.findById("user-1", created.id) }

                updated?.sessionEndTime shouldBe 4000L
                found?.sessionEndTime shouldBe 4000L
            }

            it("does not update another user's session") {
                val created = runBlocking { repository.create("user-1", createRequest) }
                val updateRequest = UpdateTrackingSessionRequest(
                    sessionStartTime = 1000L,
                    sessionEndTime = 4000L,
                    name = "Mario",
                    surname = "Rossi",
                    birthDate = "01/01/1990",
                    additionalNotes = null,
                )

                val updated = runBlocking { repository.update("user-2", created.id, updateRequest) }

                updated.shouldBeNull()
            }

            it("deletes a session") {
                val created = runBlocking { repository.create("user-1", createRequest) }

                val deleted = runBlocking { repository.delete("user-1", created.id) }
                val found = runBlocking { repository.findById("user-1", created.id) }

                deleted shouldBe true
                found.shouldBeNull()
            }

            it("does not delete another user's session") {
                val created = runBlocking { repository.create("user-1", createRequest) }

                val deleted = runBlocking { repository.delete("user-2", created.id) }
                val found = runBlocking { repository.findById("user-1", created.id) }

                deleted shouldBe false
                found shouldBe created
            }
        }
    })
