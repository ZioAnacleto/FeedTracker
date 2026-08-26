package com.zioanacleto.feedtracker.testutil

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionDataSource
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.network.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTrackingSessionDataSource(initial: List<TrackingSessionModel> = emptyList()) : TrackingSessionDataSource {
    private val sessions = initial.toMutableList()
    val saved = mutableListOf<TrackingSessionModel>()
    var getSessionsError: Throwable? = null
    var getSessionError: Throwable? = null
    var saveError: Throwable? = null
    var deleteError: Throwable? = null
    val deletedIds = mutableListOf<String>()

    override suspend fun getTrackingSessions(): List<TrackingSessionModel> {
        getSessionsError?.let { throw it }
        return sessions.toList()
    }

    override suspend fun getTrackingSession(id: String): TrackingSessionModel {
        getSessionError?.let { throw it }
        return sessions.first { it.id == id }
    }

    override suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel) {
        saveError?.let { throw it }
        saved += sessionModel
        sessions.removeAll { it.id == sessionModel.id }
        sessions += sessionModel
    }

    override suspend fun deleteTrackingSession(id: String) {
        deleteError?.let { throw it }
        if (!sessions.removeAll { it.id == id }) {
            error("Tracking session not found: $id")
        }
        deletedIds += id
    }
}

class FakeNetworkMonitor(online: Boolean) : NetworkMonitor {
    private val _isOnline = MutableStateFlow(online)
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}

object ImmediateDispatcherProvider : DispatcherProvider {
    override fun io(): CoroutineDispatcher = Dispatchers.Unconfined
    override fun main(): CoroutineDispatcher = Dispatchers.Unconfined
    override fun default(): CoroutineDispatcher = Dispatchers.Unconfined
    override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}

fun trackingSession(id: String = "session-1", name: String = "Mario") = TrackingSessionModel(
    id = id,
    sessionStartTime = 1_000L,
    sessionEndTime = 2_000L,
    name = name,
    surname = "Rossi",
    birthDate = "01/01/1990",
    additionalNotes = null,
)
