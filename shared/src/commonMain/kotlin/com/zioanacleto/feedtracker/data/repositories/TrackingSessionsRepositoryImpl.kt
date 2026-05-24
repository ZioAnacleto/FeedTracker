package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionDataSource
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TrackingSessionsRepositoryImpl(
    private val localDataSource: TrackingSessionDataSource,
    private val networkDataSource: TrackingSessionDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val networkMonitor: NetworkMonitor
) : TrackingSessionsRepository {
    override suspend fun getTrackingSessions(): Flow<Resource<List<TrackingSessionModel>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTrackingSession(id: String): Flow<Resource<TrackingSessionModel>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveTrackingSession(trackingSession: TrackingSessionModel) {
        networkMonitor.isOnline.map {
            if(it) {
                networkDataSource.saveNewTrackingSession(trackingSession)
            } else {
                localDataSource.saveNewTrackingSession(trackingSession)
            }
        }
    }
}