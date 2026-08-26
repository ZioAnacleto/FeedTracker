package com.zioanacleto.feedtracker

import com.zioanacleto.feedtracker.config.configureDI
import com.zioanacleto.feedtracker.config.configureDatabase
import com.zioanacleto.feedtracker.config.configureExceptions
import com.zioanacleto.feedtracker.config.configureRouting
import com.zioanacleto.feedtracker.config.configureSecurity
import com.zioanacleto.feedtracker.config.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureExceptions()
    configureSecurity()
    configureDI()
    configureDatabase()
    configureRouting()
}

fun Application.testModule(initDatabase: Boolean = false, configureDependencies: Application.() -> Unit = { configureDI() }) {
    configureSerialization()
    configureExceptions()
    configureSecurity()
    configureDependencies()
    if (initDatabase) {
        configureDatabase()
    }
    configureRouting()
}
