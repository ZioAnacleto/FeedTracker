package com.zioanacleto.feedtracker.config

import io.ktor.server.config.ApplicationConfig
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

interface AppConfig {
    val database: DatabaseConfig
    val auth: AuthConfig
    val smtp: SmtpConfig
}

data class DatabaseConfig(val driver: String, val url: String, val user: String, val password: String, val maxPoolSize: Int)

data class AuthConfig(
    val jwtSecret: String,
    val accessTokenTtlSeconds: Long,
    val registrationTokenTtlSeconds: Long,
    val verificationCodeTtlSeconds: Long,
    val verificationLinkBase: String,
    val googleClientId: String,
    val appleAudience: String,
)

data class SmtpConfig(
    val enabled: Boolean,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val from: String,
    val startTls: Boolean,
)

class HoconAppConfig(private val config: ApplicationConfig) : AppConfig {
    private val configuredUrl = config.property("database.url").getString()
    private val resolvedJdbcUrl = configuredUrl.toJdbcUrl()

    override val auth: AuthConfig
        get() = AuthConfig(
            jwtSecret = stringOrDefault(
                "auth.jwtSecret",
                "feedtracker-dev-jwt-secret-change-me!!",
            ),
            accessTokenTtlSeconds = longOrDefault("auth.accessTokenTtlSeconds", 604_800L),
            registrationTokenTtlSeconds = longOrDefault("auth.registrationTokenTtlSeconds", 900L),
            verificationCodeTtlSeconds = longOrDefault("auth.verificationCodeTtlSeconds", 900L),
            verificationLinkBase = stringOrDefault("auth.verificationLinkBase", "feedtracker://auth/verify"),
            googleClientId = stringOrDefault("auth.googleClientId", ""),
            appleAudience = stringOrDefault("auth.appleAudience", ""),
        )

    override val smtp: SmtpConfig
        get() = SmtpConfig(
            enabled = booleanOrDefault("smtp.enabled", true),
            host = stringOrDefault("smtp.host", "localhost"),
            port = intOrDefault("smtp.port", 1026),
            username = stringOrDefault("smtp.username", ""),
            password = stringOrDefault("smtp.password", ""),
            from = stringOrDefault("smtp.from", "noreply@feedtracker.local"),
            startTls = booleanOrDefault("smtp.startTls", false),
        )

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

    private fun resolveCredentials(userFromConfig: String?, passwordFromConfig: String?, dbUrl: String): Pair<String, String> {
        val urlCredentials = dbUrl.toUriOrNull()?.extractCredentialsFromUserInfo()
        val envUser = firstNonBlankEnv("DATABASE_USER", "PGUSER", "POSTGRES_USER", "DB_USER")
        val envPassword = firstNonBlankEnv("DATABASE_PASSWORD", "PGPASSWORD", "POSTGRES_PASSWORD", "DB_PASSWORD")

        // Railway/Heroku inject DATABASE_URL with userinfo. Those credentials must win over
        // HOCON defaults (user=feedtracker), which would otherwise always be present.
        if (urlCredentials != null && urlCredentials.first.isNotBlank()) {
            return urlCredentials
        }

        val resolvedUser = when {
            !envUser.isNullOrBlank() -> envUser
            !userFromConfig.isNullOrBlank() -> userFromConfig
            else -> throw IllegalStateException(
                "Database username is missing. Set one of: database.user, DATABASE_USER, PGUSER, POSTGRES_USER, or include user info in DATABASE_URL.",
            )
        }
        val resolvedPassword = when {
            !envPassword.isNullOrBlank() -> envPassword
            passwordFromConfig != null -> passwordFromConfig
            else -> ""
        }

        return resolvedUser to resolvedPassword
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
                    .withSslRequiredIfRemote(host)
            }

            else -> this
        }
    }

    private fun String.withSslRequiredIfRemote(host: String): String {
        if (host == "localhost" || host == "127.0.0.1" || host == "::1") return this
        if (contains("sslmode=", ignoreCase = true)) return this
        val separator = if (contains("?")) "&" else "?"
        return "${this}${separator}sslmode=require"
    }

    private fun String.toUriOrNull(): URI? = runCatching { URI(this) }.getOrNull()

    private fun URI.extractCredentialsFromUserInfo(): Pair<String, String>? {
        val userInfo = userInfo ?: return null
        val decoded = URLDecoder.decode(userInfo, StandardCharsets.UTF_8)
        val parts = decoded.split(":", limit = 2)
        val user = parts.firstOrNull().orEmpty()
        if (user.isBlank()) return null
        return user to parts.getOrElse(1) { "" }
    }

    private fun firstNonBlankEnv(vararg keys: String): String? = keys.asSequence()
        .mapNotNull { key -> System.getenv(key) }
        .firstOrNull { value -> value.isNotBlank() }

    private fun stringOrDefault(path: String, default: String): String =
        config.propertyOrNull(path)?.getString()?.takeIf { it.isNotBlank() } ?: default

    private fun longOrDefault(path: String, default: Long): Long = config.propertyOrNull(path)?.getString()?.toLongOrNull() ?: default

    private fun intOrDefault(path: String, default: Int): Int = config.propertyOrNull(path)?.getString()?.toIntOrNull() ?: default

    private fun booleanOrDefault(path: String, default: Boolean): Boolean =
        config.propertyOrNull(path)?.getString()?.toBooleanStrictOrNull() ?: default
}
