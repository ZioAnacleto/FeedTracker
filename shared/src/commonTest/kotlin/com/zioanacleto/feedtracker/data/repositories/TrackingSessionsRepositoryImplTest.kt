package com.zioanacleto.feedtracker.data.repositories

import app.cash.turbine.test
import com.zioanacleto.feedtracker.data.datasources.TrackingSessionDataSource
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.testutil.FakeNetworkMonitor
import com.zioanacleto.feedtracker.testutil.FakeTrackingSessionDataSource
import com.zioanacleto.feedtracker.testutil.ImmediateDispatcherProvider
import com.zioanacleto.feedtracker.testutil.trackingSession
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TrackingSessionsRepositoryImplTest {

    @Test
    fun getTrackingSessionsUsesNetworkWhenOnline() = runTest {
        val networkSession = trackingSession("network")
        val repository = repository(
            online = true,
            network = FakeTrackingSessionDataSource(listOf(networkSession)),
            local = FakeTrackingSessionDataSource(listOf(trackingSession("local"))),
        )

        repository.getTrackingSessions().test {
            awaitItem() shouldBe Resource.Loading
            awaitItem() shouldBe Resource.Success(listOf(networkSession))
            awaitComplete()
        }
    }

    @Test
    fun getTrackingSessionsUsesLocalWhenOffline() = runTest {
        val localSession = trackingSession("local")
        val repository = repository(
            online = false,
            network = FakeTrackingSessionDataSource(listOf(trackingSession("network"))),
            local = FakeTrackingSessionDataSource(listOf(localSession)),
        )

        repository.getTrackingSessions().test {
            awaitItem() shouldBe Resource.Loading
            awaitItem() shouldBe Resource.Success(listOf(localSession))
            awaitComplete()
        }
    }

    @Test
    fun getTrackingSessionsEmitsErrorWhenSourceFails() = runTest {
        val network = FakeTrackingSessionDataSource().apply {
            getSessionsError = IllegalStateException("network down")
        }
        val repository = repository(online = true, network = network)

        repository.getTrackingSessions().test {
            awaitItem() shouldBe Resource.Loading
            val error = awaitItem().shouldBeInstanceOf<Resource.Error>()
            error.message shouldBe "network down"
            awaitComplete()
        }
    }

    @Test
    fun getTrackingSessionUsesNetworkWhenOnline() = runTest {
        val session = trackingSession("session-1")
        val repository = repository(
            online = true,
            network = FakeTrackingSessionDataSource(listOf(session)),
        )

        repository.getTrackingSession("session-1").test {
            awaitItem() shouldBe Resource.Loading
            awaitItem() shouldBe Resource.Success(session)
            awaitComplete()
        }
    }

    @Test
    fun saveTrackingSessionPostsToNetworkWhenOnline() = runTest {
        val network = FakeTrackingSessionDataSource()
        val local = FakeTrackingSessionDataSource()
        val repository = repository(online = true, network = network, local = local)
        val session = trackingSession("new")

        repository.saveTrackingSession(session)

        network.saved shouldBe listOf(session)
        local.saved.shouldBeEmpty()
    }

    @Test
    fun saveTrackingSessionStoresLocallyWhenOffline() = runTest {
        val network = FakeTrackingSessionDataSource()
        val local = FakeTrackingSessionDataSource()
        val repository = repository(online = false, network = network, local = local)
        val session = trackingSession("offline")

        repository.saveTrackingSession(session)

        local.saved shouldBe listOf(session)
        network.saved.shouldBeEmpty()
    }

    private fun repository(
        online: Boolean,
        network: TrackingSessionDataSource = FakeTrackingSessionDataSource(),
        local: TrackingSessionDataSource = FakeTrackingSessionDataSource(),
    ) = TrackingSessionsRepositoryImpl(
        localDataSource = local,
        networkDataSource = network,
        dispatcherProvider = ImmediateDispatcherProvider,
        networkMonitor = FakeNetworkMonitor(online),
    )
}
