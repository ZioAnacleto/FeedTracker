package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel

fun TrackingSessionModel.toCreateRequest(): CreateTrackingSessionRequest = CreateTrackingSessionRequest(
    sessionStartTime = sessionStartTime,
    sessionEndTime = sessionEndTime,
    name = name,
    surname = surname,
    birthDate = birthDate,
    additionalNotes = additionalNotes,
)
