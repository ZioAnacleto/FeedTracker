package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.network.FeedTrackerApiClient
import com.zioanacleto.feedtracker.network.getServerBaseUrl

class TrackingSessionNetworkDataSource(
    private val apiClient: FeedTrackerApiClient,
) : TrackingSessionDataSource {
    override suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel) {
        apiClient.createPostRequest<CreateTrackingSessionRequest, TrackingSessionModel>(
            url = "${getServerBaseUrl()}/api/tracking-sessions",
            request = sessionModel.toCreateRequest()
        )
    }
}