package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionDataSource
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.network.NetworkMonitor
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TrackingSessionsRepositoryImplAndroidTest {

    @Test
    fun getTrackingSessionsReturnsErrorWhenDataSourceThrows() = runTest {
        val network = mockk<TrackingSessionDataSource>()
        val dispatcherProvider = mockk<DispatcherProvider>()
        val networkMonitor = mockk<NetworkMonitor>()
        every { dispatcherProvider.io() } returns Dispatchers.Unconfined
        every { networkMonitor.isOnline } returns flowOf(true)
        val local = mockk<TrackingSessionDataSource>()
        coEvery { local.getTrackingSessions() } returns emptyList()
        coEvery { network.getTrackingSessions() } throws IllegalStateException("timeout")

        val repository = TrackingSessionsRepositoryImpl(
            localDataSource = local,
            networkDataSource = network,
            dispatcherProvider = dispatcherProvider,
            networkMonitor = networkMonitor,
        )

        val result = repository.getTrackingSessions().first { it !is Resource.Loading }
        result.shouldBeInstanceOf<Resource.Error>()
        result.message shouldBe "timeout"
    }
}
