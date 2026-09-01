package com.zioanacleto.feedtracker.domain.repositories

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface TrackingSessionsRepository {
    val syncedPendingCount: Flow<Int>
        get() = emptyFlow()
    suspend fun getTrackingSessions(): Flow<Resource<List<TrackingSessionModel>>>
    suspend fun getTrackingSession(id: String): Flow<Resource<TrackingSessionModel>>
    suspend fun saveTrackingSession(trackingSession: TrackingSessionModel)
    suspend fun deleteTrackingSession(id: String)
}
