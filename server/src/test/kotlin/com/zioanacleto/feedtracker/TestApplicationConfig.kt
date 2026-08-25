package com.zioanacleto.feedtracker

import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder

fun ApplicationTestBuilder.installTestConfig() {
    environment {
        config = MapApplicationConfig(
            "database.driver" to "org.h2.Driver",
            "database.url" to "jdbc:h2:mem:feedtracker-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            "database.user" to "sa",
            "database.password" to "",
            "database.maxPoolSize" to "5",
        )
    }
}
