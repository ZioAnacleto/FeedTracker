package com.zioanacleto.feedtracker.features.trackingsessions.models

import org.jetbrains.exposed.sql.Table

object TrackingSessionsTable : Table("tracking_sessions") {
    val id = varchar("id", 36)
    val sessionStartTime = long("session_start_time")
    val sessionEndTime = long("session_end_time")
    val name = varchar("name", 255)
    val surname = varchar("surname", 255)
    val birthDate = varchar("birth_date", 10)
    val additionalNotes = text("additional_notes").nullable()

    override val primaryKey = PrimaryKey(id)
}
