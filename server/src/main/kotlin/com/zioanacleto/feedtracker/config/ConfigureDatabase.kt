package com.zioanacleto.feedtracker.config

import io.ktor.server.application.Application
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
    val appConfig by inject<AppConfig>()
    DatabaseFactory.init(appConfig.database)
}
