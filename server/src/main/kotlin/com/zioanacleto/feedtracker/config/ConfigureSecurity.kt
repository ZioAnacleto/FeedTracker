package com.zioanacleto.feedtracker.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders

fun Application.configureSecurity() {
    install(DefaultHeaders) {
        header("XContentTypeOptions", "nosniff")
        header("XFrameOptions", "DENY")
        header("ContentSecurityPolicy", "default-src 'self'")
        header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
    }
}