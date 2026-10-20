package com.zioanacleto.feedtracker.features.trackingsessions.routes

import com.zioanacleto.feedtracker.common.exceptions.ValidationException
import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest
import com.zioanacleto.feedtracker.features.auth.routes.requireBearerToken
import com.zioanacleto.feedtracker.features.auth.services.AuthService
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.trackingSessionRoutes(trackingSessionService: TrackingSessionService, authService: AuthService) {
    route("/api/tracking-sessions") {
        get {
            val userId = authService.authenticatedUserId(call.requireBearerToken())
            val sessions = trackingSessionService.getAll(userId)
            call.respond(
                ApiResponse<List<TrackingSessionModel>>(
                    status = "SUCCESS",
                    message = "Tracking sessions retrieved",
                    data = sessions,
                ),
            )
        }

        get("/{id}") {
            val userId = authService.authenticatedUserId(call.requireBearerToken())
            val id = call.parameters["id"]?.takeIf { it.isNotBlank() }
                ?: throw ValidationException("Invalid ID")
            val session = trackingSessionService.getById(userId, id)
            call.respond(ApiResponse("SUCCESS", "Tracking session retrieved", session))
        }

        post {
            val userId = authService.authenticatedUserId(call.requireBearerToken())
            val request = call.receive<CreateTrackingSessionRequest>()
            val session = trackingSessionService.create(userId, request)
            call.respond(
                HttpStatusCode.Created,
                ApiResponse("SUCCESS", "Tracking session created", session),
            )
        }

        put("/{id}") {
            val userId = authService.authenticatedUserId(call.requireBearerToken())
            val id = call.parameters["id"]?.takeIf { it.isNotBlank() }
                ?: throw ValidationException("Invalid ID")
            val request = call.receive<UpdateTrackingSessionRequest>()
            val session = trackingSessionService.update(userId, id, request)
            call.respond(ApiResponse("SUCCESS", "Tracking session updated", session))
        }

        delete("/{id}") {
            val userId = authService.authenticatedUserId(call.requireBearerToken())
            val id = call.parameters["id"]?.takeIf { it.isNotBlank() }
                ?: throw ValidationException("Invalid ID")
            trackingSessionService.delete(userId, id)
            call.respond(ApiResponse<Unit>("SUCCESS", "Tracking session deleted"))
        }
    }
}
