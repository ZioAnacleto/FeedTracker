package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.data.local.InMemoryPendingSessionsStore
import com.zioanacleto.feedtracker.data.local.PendingSessionsStore
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TrackingSessionLocalDataSource(
    private val pendingSessionsStore: PendingSessionsStore = InMemoryPendingSessionsStore(),
) : TrackingSessionDataSource {
    private val mutex = Mutex()
    private var sessions: MutableList<TrackingSessionModel>? = null

    override suspend fun getTrackingSessions(): List<TrackingSessionModel> = mutex.withLock {
        loaded().toList()
    }

    override suspend fun getTrackingSession(id: String): TrackingSessionModel = mutex.withLock {
        loaded().firstOrNull { it.id == id }
            ?: error("Tracking session not found: $id")
    }

    override suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel) {
        mutex.withLock {
            val current = loaded()
            current.removeAll { it.id == sessionModel.id }
            current.add(sessionModel)
            persistLocked(current)
        }
    }

    override suspend fun deleteTrackingSession(id: String) {
        mutex.withLock {
            val current = loaded()
            val removed = current.removeAll { it.id == id }
            if (!removed) {
                error("Tracking session not found: $id")
            }
            persistLocked(current)
        }
    }

    private fun loaded(): MutableList<TrackingSessionModel> {
        val cached = sessions
        if (cached != null) {
            return cached
        }
        val loaded = pendingSessionsStore.load().toMutableList()
        sessions = loaded
        return loaded
    }

    private fun persistLocked(current: List<TrackingSessionModel>) {
        pendingSessionsStore.save(current)
    }
}
