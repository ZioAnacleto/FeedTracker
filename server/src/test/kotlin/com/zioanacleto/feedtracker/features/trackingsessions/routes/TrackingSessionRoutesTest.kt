package com.zioanacleto.feedtracker.features.trackingsessions.routes

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.config.configureDI
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionService
import com.zioanacleto.feedtracker.installTestConfig
import com.zioanacleto.feedtracker.testModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.koin.dsl.module

class TrackingSessionRoutesTest :
    FunSpec({

        val json = Json { ignoreUnknownKeys = true }

        val session = TrackingSessionModel(
            id = "session-1",
            sessionStartTime = 1000L,
            sessionEndTime = 2000L,
            name = "Mario",
            surname = "Rossi",
            birthDate = "01/01/1990",
            additionalNotes = null,
        )

        test("GET /api/tracking-sessions/{id} returns 200 when session exists") {
            val mockService = mockk<TrackingSessionService>()
            coEvery { mockService.getById("session-1") } returns session

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(extraModules = listOf(module { single<TrackingSessionService> { mockService } }))
                    }
                }

                client.get("/api/tracking-sessions/session-1").apply {
                    status shouldBe HttpStatusCode.OK
                    val response = json.decodeFromString<ApiResponse<TrackingSessionModel>>(bodyAsText())
                    response.data shouldBe session
                }
            }
        }

        test("DELETE /api/tracking-sessions/{id} returns 200 when session is deleted") {
            val mockService = mockk<TrackingSessionService>()
            coEvery { mockService.delete("session-1") } returns Unit

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(extraModules = listOf(module { single<TrackingSessionService> { mockService } }))
                    }
                }

                client.delete("/api/tracking-sessions/session-1").apply {
                    status shouldBe HttpStatusCode.OK
                }
            }
        }
    })
