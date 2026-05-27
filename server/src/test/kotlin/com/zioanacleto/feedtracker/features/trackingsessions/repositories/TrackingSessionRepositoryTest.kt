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

class TrackingSessionRepositoryTest : DescribeSpec({

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
            val created = runBlocking { repository.create(createRequest) }
            val found = runBlocking { repository.findById(created.id) }

            found shouldBe created
        }

        it("lists all sessions") {
            runBlocking { repository.create(createRequest) }
            runBlocking { repository.create(createRequest.copy(name = "Luigi")) }

            val all = runBlocking { repository.findAll() }

            all shouldHaveSize 2
        }

        it("updates a session") {
            val created = runBlocking { repository.create(createRequest) }
            val updateRequest = UpdateTrackingSessionRequest(
                sessionStartTime = 1000L,
                sessionEndTime = 4000L,
                name = "Mario",
                surname = "Rossi",
                birthDate = "01/01/1990",
                additionalNotes = null,
            )

            val updated = runBlocking { repository.update(created.id, updateRequest) }
            val found = runBlocking { repository.findById(created.id) }

            updated?.sessionEndTime shouldBe 4000L
            found?.sessionEndTime shouldBe 4000L
        }

        it("deletes a session") {
            val created = runBlocking { repository.create(createRequest) }

            val deleted = runBlocking { repository.delete(created.id) }
            val found = runBlocking { repository.findById(created.id) }

            deleted shouldBe true
            found.shouldBeNull()
        }
    }
})
