package com.zioanacleto.feedtracker.features.trackingsessions.services

import com.zioanacleto.feedtracker.common.exceptions.ResourceNotFoundException
import com.zioanacleto.feedtracker.common.exceptions.ValidationException
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest
import com.zioanacleto.feedtracker.features.trackingsessions.repositories.TrackingSessionRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

class TrackingSessionServiceTest :
    DescribeSpec({

        describe("TrackingSessionService") {
            val repository = mockk<TrackingSessionRepository>()
            val service = TrackingSessionServiceImpl(repository)

            val session = TrackingSessionModel(
                id = "session-1",
                sessionStartTime = 1000L,
                sessionEndTime = 2000L,
                name = "Mario",
                surname = "Rossi",
                birthDate = "01/01/1990",
                additionalNotes = "note",
            )

            val createRequest = CreateTrackingSessionRequest(
                sessionStartTime = 1000L,
                sessionEndTime = 2000L,
                name = "Mario",
                surname = "Rossi",
                birthDate = "01/01/1990",
                additionalNotes = "note",
            )

            val updateRequest = UpdateTrackingSessionRequest(
                sessionStartTime = 1000L,
                sessionEndTime = 3000L,
                name = "Mario",
                surname = "Rossi",
                birthDate = "01/01/1990",
                additionalNotes = null,
            )

            it("returns session when it exists") {
                coEvery { repository.findById("user-1", "session-1") } returns session

                val result = runBlocking { service.getById("user-1", "session-1") }

                result shouldBe session
            }

            it("throws when session is not found") {
                coEvery { repository.findById("user-1", "missing") } returns null

                shouldThrow<ResourceNotFoundException> {
                    runBlocking { service.getById("user-1", "missing") }
                }
            }

            it("creates a valid session") {
                coEvery { repository.create("user-1", createRequest) } returns session

                val result = runBlocking { service.create("user-1", createRequest) }

                result shouldBe session
            }

            it("rejects invalid birth date format") {
                val invalidRequest = createRequest.copy(birthDate = "1990-01-01")

                shouldThrow<ValidationException> {
                    runBlocking { service.create("user-1", invalidRequest) }
                }
            }

            it("rejects end time before start time") {
                val invalidRequest = createRequest.copy(sessionEndTime = 500L)

                shouldThrow<ValidationException> {
                    runBlocking { service.create("user-1", invalidRequest) }
                }
            }

            it("rejects blank person fields") {
                shouldThrow<ValidationException> {
                    runBlocking { service.create("user-1", createRequest.copy(name = " ")) }
                }
            }

            it("updates an existing session") {
                val updated = session.copy(sessionEndTime = 3000L)
                coEvery { repository.update("user-1", "session-1", updateRequest) } returns updated

                val result = runBlocking { service.update("user-1", "session-1", updateRequest) }

                result shouldBe updated
            }

            it("throws when updating a missing session") {
                coEvery { repository.update("user-1", "missing", updateRequest) } returns null

                shouldThrow<ResourceNotFoundException> {
                    runBlocking { service.update("user-1", "missing", updateRequest) }
                }
            }

            it("deletes an existing session") {
                coEvery { repository.delete("user-1", "session-1") } returns true

                runBlocking { service.delete("user-1", "session-1") }
            }

            it("throws when deleting a missing session") {
                coEvery { repository.delete("user-1", "missing") } returns false

                shouldThrow<ResourceNotFoundException> {
                    runBlocking { service.delete("user-1", "missing") }
                }
            }
        }
    })
