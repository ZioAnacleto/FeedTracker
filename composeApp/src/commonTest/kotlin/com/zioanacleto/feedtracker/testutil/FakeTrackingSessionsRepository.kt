package com.zioanacleto.feedtracker.testutil

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTrackingSessionsRepository(
    private val sessions: Flow<Resource<List<TrackingSessionModel>>> = flowOf(Resource.Loading),
    private val session: Flow<Resource<TrackingSessionModel>> = flowOf(Resource.Loading),
    private val saveError: Throwable? = null,
) : TrackingSessionsRepository {
    val saved = mutableListOf<TrackingSessionModel>()

    override suspend fun getTrackingSessions(): Flow<Resource<List<TrackingSessionModel>>> = sessions

    override suspend fun getTrackingSession(id: String): Flow<Resource<TrackingSessionModel>> = session

    override suspend fun saveTrackingSession(trackingSession: TrackingSessionModel) {
        saveError?.let { throw it }
        saved += trackingSession
    }
}

fun sampleSession(id: String = "session-1", name: String = "Mario") = TrackingSessionModel(
    id = id,
    sessionStartTime = 1_000L,
    sessionEndTime = 2_000L,
    name = name,
    surname = "Rossi",
    birthDate = "01/01/1990",
    additionalNotes = null,
)
