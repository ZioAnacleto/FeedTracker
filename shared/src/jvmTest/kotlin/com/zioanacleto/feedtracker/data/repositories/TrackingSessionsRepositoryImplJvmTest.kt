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

class TrackingSessionsRepositoryImplJvmTest {

    @Test
    fun getTrackingSessionReturnsErrorWhenDataSourceThrows() = runTest {
        val network = mockk<TrackingSessionDataSource>()
        val dispatcherProvider = mockk<DispatcherProvider>()
        val networkMonitor = mockk<NetworkMonitor>()
        every { dispatcherProvider.io() } returns Dispatchers.Unconfined
        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { network.getTrackingSession("1") } throws IllegalStateException("not found")

        val repository = TrackingSessionsRepositoryImpl(
            localDataSource = mockk(),
            networkDataSource = network,
            dispatcherProvider = dispatcherProvider,
            networkMonitor = networkMonitor,
        )

        val result = repository.getTrackingSession("1").first { it !is Resource.Loading }
        result.shouldBeInstanceOf<Resource.Error>()
        result.message shouldBe "not found"
    }
}
