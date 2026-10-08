package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.network.FeedTrackerApiClient

class TrackingSessionNetworkDataSource(private val apiClient: FeedTrackerApiClient, private val accessToken: () -> String) :
    TrackingSessionDataSource {
    override suspend fun getTrackingSessions(): List<TrackingSessionModel> = apiClient.getTrackingSessions(accessToken())

    override suspend fun getTrackingSession(id: String): TrackingSessionModel = apiClient.getTrackingSession(accessToken(), id)

    override suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel) {
        apiClient.createTrackingSession(accessToken(), sessionModel.toCreateRequest())
    }

    override suspend fun deleteTrackingSession(id: String) {
        apiClient.deleteTrackingSession(accessToken(), id)
    }

    override suspend fun clear() = Unit
}
