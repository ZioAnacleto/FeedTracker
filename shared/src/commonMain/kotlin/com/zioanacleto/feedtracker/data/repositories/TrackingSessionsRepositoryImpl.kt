package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionDataSource
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TrackingSessionsRepositoryImpl(
    private val localDataSource: TrackingSessionDataSource,
    private val networkDataSource: TrackingSessionDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val networkMonitor: NetworkMonitor,
) : TrackingSessionsRepository {
    override suspend fun getTrackingSessions(): Flow<Resource<List<TrackingSessionModel>>> =
        loadFromPreferredSource { getTrackingSessions() }

    override suspend fun getTrackingSession(id: String): Flow<Resource<TrackingSessionModel>> =
        loadFromPreferredSource { getTrackingSession(id) }

    override suspend fun saveTrackingSession(trackingSession: TrackingSessionModel) {
        withContext(dispatcherProvider.io()) {
            preferredDataSource().saveNewTrackingSession(trackingSession)
        }
    }

    override suspend fun deleteTrackingSession(id: String) {
        withContext(dispatcherProvider.io()) {
            preferredDataSource().deleteTrackingSession(id)
        }
    }

    private fun <T> loadFromPreferredSource(block: suspend TrackingSessionDataSource.() -> T): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(preferredDataSource().block()))
    }.catch { throwable ->
        emit(Resource.Error(throwable.message ?: "Unknown error"))
    }.flowOn(dispatcherProvider.io())

    private suspend fun preferredDataSource(): TrackingSessionDataSource =
        if (networkMonitor.isOnline.first()) networkDataSource else localDataSource
}
