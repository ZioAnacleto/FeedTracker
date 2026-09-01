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
        )

        repository.getTrackingSessions().test {
            awaitItem() shouldBe Resource.Loading
            awaitItem() shouldBe Resource.Success(listOf(networkSession))
            cancelAndIgnoreRemainingEvents()
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
            cancelAndIgnoreRemainingEvents()
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
            cancelAndIgnoreRemainingEvents()
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
        local.getTrackingSessions().shouldBeEmpty()
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

    @Test
    fun deleteTrackingSessionUsesNetworkWhenOnline() = runTest {
        val session = trackingSession("to-delete")
        val network = FakeTrackingSessionDataSource(listOf(session))
        val local = FakeTrackingSessionDataSource(listOf(session))
        val repository = repository(online = true, network = network, local = local)

        repository.deleteTrackingSession(session.id)

        network.deletedIds shouldBe listOf(session.id)
        local.deletedIds.shouldBeEmpty()
    }

    @Test
    fun deleteTrackingSessionUsesLocalWhenOffline() = runTest {
        val session = trackingSession("offline-delete")
        val network = FakeTrackingSessionDataSource(listOf(session))
        val local = FakeTrackingSessionDataSource(listOf(session))
        val repository = repository(online = false, network = network, local = local)

        repository.deleteTrackingSession(session.id)

        local.deletedIds shouldBe listOf(session.id)
        network.deletedIds.shouldBeEmpty()
    }

    @Test
    fun syncsPendingLocalSessionsWhenConnectivityReturns() = runTest {
        val network = FakeTrackingSessionDataSource()
        val local = FakeTrackingSessionDataSource()
        val networkMonitor = FakeNetworkMonitor(online = false)
        val repository = repository(
            networkMonitor = networkMonitor,
            network = network,
            local = local,
        )
        val session = trackingSession("offline")

        repository.saveTrackingSession(session)
        network.saved.shouldBeEmpty()

        networkMonitor.setOnline(true)
        repository.getTrackingSessions().test {
            awaitItem() shouldBe Resource.Loading
            awaitItem() shouldBe Resource.Success(listOf(session))
            cancelAndIgnoreRemainingEvents()
        }

        network.saved shouldBe listOf(session)
        local.getTrackingSessions().shouldBeEmpty()
    }

    @Test
    fun emitsSyncedCountWhenPendingSessionsAreUploaded() = runTest {
        val network = FakeTrackingSessionDataSource()
        val local = FakeTrackingSessionDataSource()
        val networkMonitor = FakeNetworkMonitor(online = false)
        val repository = repository(
            networkMonitor = networkMonitor,
            network = network,
            local = local,
        )
        val session = trackingSession("offline")

        repository.saveTrackingSession(session)
        networkMonitor.setOnline(true)

        repository.syncedPendingCount.test {
            repository.getTrackingSessions().test {
                awaitItem() shouldBe Resource.Loading
                awaitItem() shouldBe Resource.Success(listOf(session))
                cancelAndIgnoreRemainingEvents()
            }
            awaitItem() shouldBe 1
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveWhileOnlineFlushesPendingLocalSessionsFirst() = runTest {
        val network = FakeTrackingSessionDataSource()
        val local = FakeTrackingSessionDataSource()
        val networkMonitor = FakeNetworkMonitor(online = false)
        val repository = repository(
            networkMonitor = networkMonitor,
            network = network,
            local = local,
        )
        val pending = trackingSession("pending")
        val onlineSession = trackingSession("online")

        repository.saveTrackingSession(pending)
        networkMonitor.setOnline(true)
        repository.saveTrackingSession(onlineSession)

        network.saved shouldBe listOf(pending, onlineSession)
        local.getTrackingSessions().shouldBeEmpty()
    }

    @Test
    fun keepsFailedSessionsLocallyAndStillListsThemWhenOnline() = runTest {
        val network = FakeTrackingSessionDataSource().apply {
            saveError = IllegalStateException("upload failed")
        }
        val local = FakeTrackingSessionDataSource()
        val networkMonitor = FakeNetworkMonitor(online = false)
        val repository = repository(
            networkMonitor = networkMonitor,
            network = network,
            local = local,
        )
        val session = trackingSession("offline")

        repository.saveTrackingSession(session)
        networkMonitor.setOnline(true)

        repository.getTrackingSessions().test {
            awaitItem() shouldBe Resource.Loading
            awaitItem() shouldBe Resource.Success(listOf(session))
            cancelAndIgnoreRemainingEvents()
        }

        local.getTrackingSessions() shouldBe listOf(session)
    }

    @Test
    fun saveFallsBackToLocalWhenNetworkIsUnreachable() = runTest {
        val network = FakeTrackingSessionDataSource().apply {
            saveError = IllegalStateException("unreachable")
        }
        val local = FakeTrackingSessionDataSource()
        val repository = repository(online = true, network = network, local = local)
        val session = trackingSession("offline")

        repository.saveTrackingSession(session)

        network.saved.shouldBeEmpty()
        local.getTrackingSessions() shouldBe listOf(session)
    }

    @Test
    fun getTrackingSessionsFallsBackToLocalWhenNetworkListFails() = runTest {
        val localSession = trackingSession("local")
        val network = FakeTrackingSessionDataSource().apply {
            getSessionsError = IllegalStateException("network down")
        }
        val repository = repository(
            online = true,
            network = network,
            local = FakeTrackingSessionDataSource(listOf(localSession)),
        )

        repository.getTrackingSessions().test {
            awaitItem() shouldBe Resource.Loading
            awaitItem() shouldBe Resource.Success(listOf(localSession))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun repository(
        online: Boolean = true,
        network: TrackingSessionDataSource = FakeTrackingSessionDataSource(),
        local: TrackingSessionDataSource = FakeTrackingSessionDataSource(),
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(online),
    ) = TrackingSessionsRepositoryImpl(
        localDataSource = local,
        networkDataSource = network,
        dispatcherProvider = ImmediateDispatcherProvider,
        networkMonitor = networkMonitor,
    )
}
