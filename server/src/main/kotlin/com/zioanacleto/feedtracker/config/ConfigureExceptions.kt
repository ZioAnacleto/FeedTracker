package com.zioanacleto.feedtracker.config

import com.zioanacleto.feedtracker.common.exceptions.ApplicationException
import com.zioanacleto.feedtracker.common.exceptions.ResourceNotFoundException
import com.zioanacleto.feedtracker.common.exceptions.ValidationException
import com.zioanacleto.feedtracker.common.models.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

private val logger = KotlinLogging.logger {}

fun Application.configureExceptions() {
    install(StatusPages) {
        exception<ResourceNotFoundException> { call, cause ->
            call.respond(
                cause.statusCode,
                ApiResponse<Unit>("ERROR", cause.message ?: "Resource not found"),
            )
        }
        exception<ValidationException> { call, cause ->
            call.respond(
                cause.statusCode,
                ApiResponse<Unit>("ERROR", cause.message ?: "Validation error"),
            )
        }
        exception<ApplicationException> { call, cause ->
            call.respond(
                cause.statusCode,
                ApiResponse<Unit>("ERROR", cause.message ?: "Application error"),
            )
        }
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception" }
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>("ERROR", "An internal error occurred"),
            )
        }
    }
}
