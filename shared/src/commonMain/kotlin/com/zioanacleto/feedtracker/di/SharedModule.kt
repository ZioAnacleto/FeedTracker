package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionLocalDataSource
import com.zioanacleto.feedtracker.data.datasources.TrackingSessionNetworkDataSource
import com.zioanacleto.feedtracker.data.local.AuthSessionStore
import com.zioanacleto.feedtracker.data.local.TrackingPreferencesStore
import com.zioanacleto.feedtracker.data.repositories.AuthRepositoryImpl
import com.zioanacleto.feedtracker.data.repositories.AuthSessionRepositoryImpl
import com.zioanacleto.feedtracker.data.repositories.TrackingPreferencesRepositoryImpl
import com.zioanacleto.feedtracker.data.repositories.TrackingSessionsRepositoryImpl
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.core.DispatcherProviderImpl
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import com.zioanacleto.feedtracker.domain.repositories.TrackingPreferencesRepository
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.network.FeedTrackerApiClient
import com.zioanacleto.feedtracker.network.createFeedTrackerHttpClient
import org.koin.dsl.module

val sharedModule = module {
    single<DispatcherProvider> { DispatcherProviderImpl() }

    single { createFeedTrackerHttpClient() }
    single { FeedTrackerApiClient(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<AuthSessionRepository> { AuthSessionRepositoryImpl(get<AuthSessionStore>()) }
    single<TrackingPreferencesRepository> {
        TrackingPreferencesRepositoryImpl(
            store = get<TrackingPreferencesStore>(),
            authRepository = get(),
            authSessionRepository = get(),
        )
    }

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
