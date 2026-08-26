package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TrackingSessionLocalDataSource : TrackingSessionDataSource {
    private val mutex = Mutex()
    private val sessions = mutableListOf<TrackingSessionModel>()

    override suspend fun getTrackingSessions(): List<TrackingSessionModel> = mutex.withLock { sessions.toList() }

    override suspend fun getTrackingSession(id: String): TrackingSessionModel = mutex.withLock {
        sessions.firstOrNull { it.id == id }
            ?: error("Tracking session not found: $id")
    }

    override suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel) {
        mutex.withLock {
            sessions.removeAll { it.id == sessionModel.id }
            sessions.add(sessionModel)
        }
    }
}
