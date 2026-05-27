package com.zioanacleto.feedtracker.config

import com.zioanacleto.feedtracker.features.trackingsessions.repositories.TrackingSessionRepository
import com.zioanacleto.feedtracker.features.trackingsessions.repositories.TrackingSessionRepositoryImpl
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionService
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionServiceImpl
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI(extraModules: List<Module> = emptyList()) {
    val appConfig = HoconAppConfig(environment.config)

    install(Koin) {
        allowOverride(true)
        slf4jLogger()
        modules(
            module {
                single<AppConfig> { appConfig }
                single { appConfig.database }
                single<TrackingSessionRepository> { TrackingSessionRepositoryImpl() }
                single<TrackingSessionService> { TrackingSessionServiceImpl(get()) }
            } + extraModules,
        )
    }
}
