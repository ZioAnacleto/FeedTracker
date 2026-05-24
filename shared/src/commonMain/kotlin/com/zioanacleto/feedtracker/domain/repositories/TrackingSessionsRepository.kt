package com.zioanacleto.feedtracker.domain.repositories

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import kotlinx.coroutines.flow.Flow

interface TrackingSessionsRepository {
    suspend fun getTrackingSessions(): Flow<Resource<List<TrackingSessionModel>>>
    suspend fun getTrackingSession(id: String): Flow<Resource<TrackingSessionModel>>
    suspend fun saveTrackingSession(trackingSession: TrackingSessionModel)
}