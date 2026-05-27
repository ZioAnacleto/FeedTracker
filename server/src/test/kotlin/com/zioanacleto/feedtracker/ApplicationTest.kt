package com.zioanacleto.feedtracker

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.common.models.HealthStatus
import com.zioanacleto.feedtracker.config.configureDI
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionService
import org.koin.dsl.module
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class ApplicationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun healthEndpointReturnsUpStatus() = testApplication {
        val mockService = mockk<TrackingSessionService>()
        coEvery { mockService.getAll() } returns emptyList()

        application {
            testModule {
                configureDI(extraModules = listOf(module { single<TrackingSessionService> { mockService } }))
            }
        }

        client.get("/health").apply {
            status shouldBe HttpStatusCode.OK
            val response = json.decodeFromString<ApiResponse<HealthStatus>>(bodyAsText())
            response.data?.status shouldBe "UP"
        }
    }
}
