package com.zioanacleto.feedtracker.config

import io.ktor.server.config.ApplicationConfig
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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

class HoconAppConfig(private val config: ApplicationConfig) : AppConfig {
    private val configuredUrl = config.property("database.url").getString()
    private val resolvedJdbcUrl = configuredUrl.toJdbcUrl()

    override val database: DatabaseConfig
        get() {
            val (resolvedUser, resolvedPassword) = resolveCredentials(
                userFromConfig = config.propertyOrNull("database.user")?.getString(),
                passwordFromConfig = config.propertyOrNull("database.password")?.getString(),
                dbUrl = configuredUrl,
            )

            return DatabaseConfig(
                driver = config.property("database.driver").getString(),
                url = resolvedJdbcUrl,
                user = resolvedUser,
                password = resolvedPassword,
                maxPoolSize = config.propertyOrNull("database.maxPoolSize")?.getString()?.toInt()
                    ?: 10,
            )
        }

    private fun resolveCredentials(
        userFromConfig: String?,
        passwordFromConfig: String?,
        dbUrl: String,
    ): Pair<String, String> {
        if (!userFromConfig.isNullOrBlank()) {
            return userFromConfig to (passwordFromConfig ?: "")
        }

        val parsedUri = dbUrl.toUriOrNull() ?: return "" to ""
        val userInfo = parsedUri.userInfo ?: return "" to ""
        val decoded = URLDecoder.decode(userInfo, StandardCharsets.UTF_8)
        val parts = decoded.split(":", limit = 2)
        return parts[0] to parts.getOrElse(1) { "" }
    }

    private fun String.toJdbcUrl(): String {
        if (startsWith("jdbc:")) return this
        val parsedUri = toUriOrNull() ?: return this
        return when (parsedUri.scheme?.lowercase()) {
            "postgres", "postgresql" -> {
                val host = parsedUri.host ?: return this
                val port = if (parsedUri.port == -1) 5432 else parsedUri.port
                val path = parsedUri.path ?: ""
                val query = parsedUri.query?.let { "?$it" } ?: ""
                "jdbc:postgresql://$host:$port$path$query"
            }

            else -> this
        }
    }

    private fun String.toUriOrNull(): URI? =
        runCatching { URI(this) }.getOrNull()
}
