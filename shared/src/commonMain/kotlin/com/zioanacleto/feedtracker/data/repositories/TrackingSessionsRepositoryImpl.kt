package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionDataSource
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.network.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TrackingSessionsRepositoryImpl(
    private val localDataSource: TrackingSessionDataSource,
    private val networkDataSource: TrackingSessionDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val networkMonitor: NetworkMonitor,
) : TrackingSessionsRepository {
    private val syncMutex = Mutex()
    private val _syncedPendingCount = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    override val syncedPendingCount: SharedFlow<Int> = _syncedPendingCount.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getTrackingSessions(): Flow<Resource<List<TrackingSessionModel>>> =
        networkMonitor.isOnline
            .distinctUntilChanged()
            .flatMapLatest { online ->
                flow {
                    emit(Resource.Loading)
                    emit(Resource.Success(loadSessions(online)))
                }.catch { throwable ->
                    emit(Resource.Error(throwable.message ?: "Unknown error"))
                }
            }
            .flowOn(dispatcherProvider.io())

    override suspend fun getTrackingSession(id: String): Flow<Resource<TrackingSessionModel>> =
        loadFromPreferredSource { getTrackingSession(id) }

    override suspend fun saveTrackingSession(trackingSession: TrackingSessionModel) {
        withContext(dispatcherProvider.io()) {
            localDataSource.saveNewTrackingSession(trackingSession)
            if (isOnline()) {
                syncPendingSessions()
            }
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

    private suspend fun loadSessions(online: Boolean): List<TrackingSessionModel> {
        if (!online) {
            return localDataSource.getTrackingSessions()
        }
        val pendingSnapshot = localDataSource.getTrackingSessions()
        return try {
            syncPendingSessions()
            val remote = networkDataSource.getTrackingSessions()
            val pending = localDataSource.getTrackingSessions()
            if (pending.isEmpty()) {
                remote
            } else {
                val remoteIds = remote.map { it.id }.toSet()
                remote + pending.filter { it.id !in remoteIds }
            }
        } catch (throwable: Throwable) {
            val pending = runCatching { localDataSource.getTrackingSessions() }.getOrDefault(emptyList())
            pending.ifEmpty { pendingSnapshot }.ifEmpty { throw throwable }
        }
    }

    private suspend fun syncPendingSessions() {
        syncMutex.withLock {
            if (!isOnline()) {
                return
            }
            val pending = localDataSource.getTrackingSessions()
            var uploaded = 0
            pending.forEach { session ->
                val synced = runCatching {
                    networkDataSource.saveNewTrackingSession(session)
                    localDataSource.deleteTrackingSession(session.id)
                }.isSuccess
                if (synced) {
                    uploaded += 1
                }
            }
            if (uploaded > 0) {
                _syncedPendingCount.tryEmit(uploaded)
            }
        }
    }

    private suspend fun isOnline(): Boolean = networkMonitor.isOnline.first()

    private suspend fun preferredDataSource(): TrackingSessionDataSource =
        if (isOnline()) networkDataSource else localDataSource
}
