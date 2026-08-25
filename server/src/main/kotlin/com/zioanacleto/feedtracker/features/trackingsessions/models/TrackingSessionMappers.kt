package com.zioanacleto.feedtracker.features.trackingsessions.models

import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

fun ResultRow.toTrackingSessionModel(): TrackingSessionModel = TrackingSessionModel(
    id = this[TrackingSessionsTable.id],
    sessionStartTime = this[TrackingSessionsTable.sessionStartTime],
    sessionEndTime = this[TrackingSessionsTable.sessionEndTime],
    name = this[TrackingSessionsTable.name],
    surname = this[TrackingSessionsTable.surname],
    birthDate = this[TrackingSessionsTable.birthDate],
    additionalNotes = this[TrackingSessionsTable.additionalNotes],
)

fun CreateTrackingSessionRequest.toModel(id: String = UUID.randomUUID().toString()): TrackingSessionModel = TrackingSessionModel(
    id = id,
    sessionStartTime = sessionStartTime,
    sessionEndTime = sessionEndTime,
    name = name,
    surname = surname,
    birthDate = birthDate,
    additionalNotes = additionalNotes,
)

fun UpdateTrackingSessionRequest.toModel(id: String): TrackingSessionModel = TrackingSessionModel(
    id = id,
    sessionStartTime = sessionStartTime,
    sessionEndTime = sessionEndTime,
    name = name,
    surname = surname,
    birthDate = birthDate,
    additionalNotes = additionalNotes,
)
