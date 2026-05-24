package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.domain.TrackingSessionModel

interface TrackingSessionDataSource {
    fun saveNewTrackingSession(sessionModel: TrackingSessionModel)
}