package com.zioanacleto.feedtracker.testutil

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTrackingSessionsRepository(
    private val sessions: Flow<Resource<List<TrackingSessionModel>>> = flowOf(Resource.Loading),
    private val session: Flow<Resource<TrackingSessionModel>> = flowOf(Resource.Loading),
    private val saveError: Throwable? = null,
    private val deleteError: Throwable? = null,
    private val deleteSuspends: Boolean = false,
) : TrackingSessionsRepository {
    val saved = mutableListOf<TrackingSessionModel>()
    val deletedIds = mutableListOf<String>()

    override suspend fun getTrackingSessions(): Flow<Resource<List<TrackingSessionModel>>> = sessions

    override suspend fun getTrackingSession(id: String): Flow<Resource<TrackingSessionModel>> = session

    override suspend fun saveTrackingSession(trackingSession: TrackingSessionModel) {
        saveError?.let { throw it }
        saved += trackingSession
    }

    override suspend fun deleteTrackingSession(id: String) {
        deleteError?.let { throw it }
        if (deleteSuspends) {
            delay(1)
        }
        deletedIds += id
    }
}

fun sampleSession(
    id: String = "session-1",
    name: String = "Mario",
    surname: String = "Rossi",
    sessionStartTime: Long = 1_000L,
    sessionEndTime: Long = 2_000L,
) = TrackingSessionModel(
    id = id,
    sessionStartTime = sessionStartTime,
    sessionEndTime = sessionEndTime,
    name = name,
    surname = surname,
    birthDate = "01/01/1990",
    additionalNotes = null,
)
