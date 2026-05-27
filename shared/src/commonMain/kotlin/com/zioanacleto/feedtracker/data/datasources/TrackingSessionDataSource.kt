package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.domain.TrackingSessionModel

interface TrackingSessionDataSource {
    suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel)
}