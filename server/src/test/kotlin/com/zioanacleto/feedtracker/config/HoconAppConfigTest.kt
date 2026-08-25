package com.zioanacleto.feedtracker.config

import io.kotest.matchers.shouldBe
import io.ktor.server.config.MapApplicationConfig
import org.junit.jupiter.api.Test

class HoconAppConfigTest {

    @Test
    fun railwayDatabaseUrlCredentialsOverrideHoconDefaults() {
        val database = HoconAppConfig(
            MapApplicationConfig(
                "database.driver" to "org.postgresql.Driver",
                "database.url" to "postgresql://postgres:s3cret@containers.railway.app:5432/railway",
                "database.user" to "feedtracker",
                "database.password" to "feedtracker",
                "database.maxPoolSize" to "10",
            ),
        ).database

        database.user shouldBe "postgres"
        database.password shouldBe "s3cret"
        database.url shouldBe "jdbc:postgresql://containers.railway.app:5432/railway?sslmode=require"
    }

    @Test
    fun jdbcUrlWithoutUserInfoKeepsHoconCredentials() {
        val database = HoconAppConfig(
            MapApplicationConfig(
                "database.driver" to "org.postgresql.Driver",
                "database.url" to "jdbc:postgresql://localhost:5433/feedtracker",
                "database.user" to "feedtracker",
                "database.password" to "feedtracker",
                "database.maxPoolSize" to "10",
            ),
        ).database

        database.user shouldBe "feedtracker"
        database.password shouldBe "feedtracker"
        database.url shouldBe "jdbc:postgresql://localhost:5433/feedtracker"
    }

    @Test
    fun urlEncodedPasswordInDatabaseUrlIsDecoded() {
        val database = HoconAppConfig(
            MapApplicationConfig(
                "database.driver" to "org.postgresql.Driver",
                "database.url" to "postgres://postgres:p%40ss%3Aword@db.example.com:5432/app",
                "database.user" to "feedtracker",
                "database.password" to "feedtracker",
            ),
        ).database

        database.user shouldBe "postgres"
        database.password shouldBe "p@ss:word"
        database.url shouldBe "jdbc:postgresql://db.example.com:5432/app?sslmode=require"
    }
}
