package com.zioanacleto.feedtracker.features.auth.routes

import com.zioanacleto.feedtracker.common.exceptions.UnauthorizedException
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header

fun ApplicationCall.requireBearerToken(): String {
    val header = request.header(HttpHeaders.Authorization)?.trim().orEmpty()
    if (!header.startsWith("Bearer ", ignoreCase = true)) {
        throw UnauthorizedException("Missing access token")
    }
    return header.substringAfter(' ').trim().ifBlank {
        throw UnauthorizedException("Missing access token")
    }
}
