package com.zioanacleto.feedtracker.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCodesTable
import com.zioanacleto.feedtracker.features.auth.models.RevokedAccessTokensTable
import com.zioanacleto.feedtracker.features.auth.models.UserAuthMethodsTable
import com.zioanacleto.feedtracker.features.auth.models.UsersTable
import com.zioanacleto.feedtracker.features.trackingpreferences.models.TrackingPreferencesTable
import com.zioanacleto.feedtracker.features.trackingsessions.models.TrackingSessionsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: DatabaseConfig) {
        val dataSource = HikariDataSource(
            HikariConfig().apply {
                driverClassName = config.driver
                jdbcUrl = config.url
                username = config.user
                password = config.password
                maximumPoolSize = config.maxPoolSize
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            },
        )
        Database.connect(dataSource)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                TrackingSessionsTable,
                UsersTable,
                EmailVerificationCodesTable,
                UserAuthMethodsTable,
                RevokedAccessTokensTable,
                TrackingPreferencesTable,
            )
        }
    }
}
