package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.network.FeedTrackerApiClient

class TrackingSessionNetworkDataSource(private val apiClient: FeedTrackerApiClient) : TrackingSessionDataSource {
    override suspend fun getTrackingSessions(): List<TrackingSessionModel> = apiClient.getTrackingSessions()

    override suspend fun getTrackingSession(id: String): TrackingSessionModel = apiClient.getTrackingSession(id)

    override suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel) {
        apiClient.createTrackingSession(sessionModel.toCreateRequest())
    }

    override suspend fun deleteTrackingSession(id: String) {
        apiClient.deleteTrackingSession(id)
    }
}
