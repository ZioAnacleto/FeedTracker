package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionLocalDataSource
import com.zioanacleto.feedtracker.data.datasources.TrackingSessionNetworkDataSource
import com.zioanacleto.feedtracker.data.repositories.TrackingSessionsRepositoryImpl
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.core.DispatcherProviderImpl
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.network.FeedTrackerApiClient
import com.zioanacleto.feedtracker.network.createFeedTrackerHttpClient
import org.koin.dsl.module

val sharedModule = module {
    single<DispatcherProvider> { DispatcherProviderImpl() }

    single { createFeedTrackerHttpClient() }
    single { FeedTrackerApiClient(get()) }

    single { TrackingSessionNetworkDataSource(get()) }
    single { TrackingSessionLocalDataSource(get()) }

    single<TrackingSessionsRepository> {
        TrackingSessionsRepositoryImpl(
            localDataSource = get<TrackingSessionLocalDataSource>(),
            networkDataSource = get<TrackingSessionNetworkDataSource>(),
            dispatcherProvider = get(),
            networkMonitor = get(),
        )
    }
}
