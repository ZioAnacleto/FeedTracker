package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.domain.TrackingSessionModel

interface TrackingSessionDataSource {
    suspend fun getTrackingSessions(): List<TrackingSessionModel>
    suspend fun getTrackingSession(id: String): TrackingSessionModel
    suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel)
    suspend fun deleteTrackingSession(id: String)
}
