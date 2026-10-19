package com.zioanacleto.feedtracker.config

import com.zioanacleto.feedtracker.features.trackingsessions.models.TrackingSessionsTable
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseMigratorTest :
    DescribeSpec({

        beforeSpec {
            Database.connect(
                url = "jdbc:h2:mem:feedtracker-migrations;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                driver = "org.h2.Driver",
                user = "sa",
                password = "",
            )
        }

        it("deletes tracking sessions with a null user id and keeps sessions that belong to a user") {
            transaction {
                exec("DROP TABLE IF EXISTS schema_migrations")
                exec("DROP TABLE IF EXISTS tracking_sessions")
                SchemaUtils.create(TrackingSessionsTable, SchemaMigrationsTable)
                TrackingSessionsTable.insert {
                    it[id] = "legacy-session"
                    it[userId] = null
                    it[sessionStartTime] = 1000L
                    it[sessionEndTime] = 2000L
                    it[name] = "Mario"
                    it[surname] = "Rossi"
                    it[birthDate] = "01/01/1990"
                    it[additionalNotes] = null
                }
                TrackingSessionsTable.insert {
                    it[id] = "owned-session"
                    it[userId] = "user-1"
                    it[sessionStartTime] = 1000L
                    it[sessionEndTime] = 2000L
                    it[name] = "Anna"
                    it[surname] = "Bianchi"
                    it[birthDate] = "02/02/1991"
                    it[additionalNotes] = null
                }
                DatabaseMigrator.applyPending()
            }

            transaction {
                TrackingSessionsTable.selectAll().map { it[TrackingSessionsTable.id] } shouldContainExactly listOf("owned-session")
            }

            transaction {
                DatabaseMigrator.applyPending()
            }

            transaction {
                TrackingSessionsTable.selectAll().map { it[TrackingSessionsTable.id] } shouldContainExactly listOf("owned-session")
            }
        }
    })
