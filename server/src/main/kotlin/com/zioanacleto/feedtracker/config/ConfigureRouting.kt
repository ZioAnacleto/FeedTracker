package com.zioanacleto.feedtracker.config

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.common.models.HealthStatus
import com.zioanacleto.feedtracker.features.auth.routes.authRoutes
import com.zioanacleto.feedtracker.features.auth.services.AuthService
import com.zioanacleto.feedtracker.features.trackingsessions.routes.trackingSessionRoutes
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionService
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

private val applicationStartTime = System.currentTimeMillis()

fun Application.configureRouting() {
    val trackingSessionService by inject<TrackingSessionService>()
    val authService by inject<AuthService>()

    routing {
        get("/health") {
            val uptimeSeconds = (System.currentTimeMillis() - applicationStartTime) / 1000
            call.respond(
                ApiResponse(
                    status = "SUCCESS",
                    message = "Service is healthy",
                    data = HealthStatus(status = "UP", uptime = "${uptimeSeconds}s"),
                ),
            )
        }

        authRoutes(authService)
        trackingSessionRoutes(trackingSessionService)
    }
}
