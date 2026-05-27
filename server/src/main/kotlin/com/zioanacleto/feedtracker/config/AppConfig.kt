package com.zioanacleto.feedtracker.config

import io.ktor.server.config.ApplicationConfig

interface AppConfig {
    val database: DatabaseConfig
}

data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
)

class HoconAppConfig(config: ApplicationConfig) : AppConfig {
    override val database: DatabaseConfig = DatabaseConfig(
        driver = config.property("database.driver").getString(),
        url = config.property("database.url").getString(),
        user = config.property("database.user").getString(),
        password = config.property("database.password").getString(),
        maxPoolSize = config.propertyOrNull("database.maxPoolSize")?.getString()?.toInt() ?: 10,
    )
}
