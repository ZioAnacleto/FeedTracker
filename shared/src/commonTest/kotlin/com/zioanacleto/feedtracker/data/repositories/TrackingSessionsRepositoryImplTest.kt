package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionDataSource
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.network.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TrackingSessionsRepositoryImplTest {

    @Test
    fun getTrackingSessionsUsesNetworkWhenOnline() = runBlocking {
        val networkSession = session("network")
        val repository = repository(
            online = true,
            network = FakeTrackingSessionDataSource(listOf(networkSession)),
            local = FakeTrackingSessionDataSource(listOf(session("local"))),
        )

        val result = repository.getTrackingSessions().first { it !is Resource.Loading }

        assertIs<Resource.Success<List<TrackingSessionModel>>>(result)
        assertEquals(listOf(networkSession), result.data)
    }

    @Test
    fun getTrackingSessionsUsesLocalWhenOffline() = runBlocking {
        val localSession = session("local")
        val repository = repository(
            online = false,
            network = FakeTrackingSessionDataSource(listOf(session("network"))),
            local = FakeTrackingSessionDataSource(listOf(localSession)),
        )

        val result = repository.getTrackingSessions().first { it !is Resource.Loading }

        assertIs<Resource.Success<List<TrackingSessionModel>>>(result)
        assertEquals(listOf(localSession), result.data)
    }

    @Test
    fun saveTrackingSessionPostsToNetworkWhenOnline() = runBlocking {
        val network = FakeTrackingSessionDataSource()
        val local = FakeTrackingSessionDataSource()
        val repository = repository(online = true, network = network, local = local)
        val session = session("new")

        repository.saveTrackingSession(session)

        assertEquals(listOf(session), network.saved)
        assertTrue(local.saved.isEmpty())
    }

    private fun repository(
        online: Boolean,
        network: TrackingSessionDataSource,
        local: TrackingSessionDataSource,
    ) = TrackingSessionsRepositoryImpl(
        localDataSource = local,
        networkDataSource = network,
        dispatcherProvider = ImmediateDispatcherProvider,
        networkMonitor = FakeNetworkMonitor(online),
    )

    private fun session(id: String) = TrackingSessionModel(
        id = id,
        sessionStartTime = 1_000L,
        sessionEndTime = 2_000L,
        name = "Mario",
        surname = "Rossi",
        birthDate = "01/01/1990",
        additionalNotes = null,
    )
}

private class FakeTrackingSessionDataSource(
    initial: List<TrackingSessionModel> = emptyList(),
) : TrackingSessionDataSource {
    private val sessions = initial.toMutableList()
    val saved = mutableListOf<TrackingSessionModel>()

    override suspend fun getTrackingSessions(): List<TrackingSessionModel> = sessions.toList()

    override suspend fun getTrackingSession(id: String): TrackingSessionModel =
        sessions.first { it.id == id }

    override suspend fun saveNewTrackingSession(sessionModel: TrackingSessionModel) {
        saved += sessionModel
        sessions += sessionModel
    }
}

private class FakeNetworkMonitor(online: Boolean) : NetworkMonitor {
    override val isOnline: Flow<Boolean> = flowOf(online)
}

private object ImmediateDispatcherProvider : DispatcherProvider {
    override fun io(): CoroutineDispatcher = Dispatchers.Unconfined
    override fun main(): CoroutineDispatcher = Dispatchers.Unconfined
    override fun default(): CoroutineDispatcher = Dispatchers.Unconfined
    override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}
