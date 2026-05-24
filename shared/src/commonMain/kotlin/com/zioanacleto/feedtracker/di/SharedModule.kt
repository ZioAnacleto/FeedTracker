package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionDataSource
import com.zioanacleto.feedtracker.data.datasources.TrackingSessionLocalDataSource
import com.zioanacleto.feedtracker.data.datasources.TrackingSessionNetworkDataSource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.data.repositories.TrackingSessionsRepositoryImpl
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.core.DispatcherProviderImpl
import org.koin.dsl.module

val sharedModule = module {
    single<DispatcherProvider> { DispatcherProviderImpl() }

    factory { TrackingSessionNetworkDataSource() }
    factory { TrackingSessionLocalDataSource() }

    factory<TrackingSessionsRepository> {
        TrackingSessionsRepositoryImpl(
            localDataSource = get(getNamedClass<TrackingSessionLocalDataSource>()),
            networkDataSource = get(getNamedClass<TrackingSessionNetworkDataSource>()),
            dispatcherProvider = get(),
            networkMonitor = get()
        )
    }
    factoryNamedClass<TrackingSessionDataSource, TrackingSessionNetworkDataSource>()
    factoryNamedClass<TrackingSessionDataSource, TrackingSessionLocalDataSource>()
}
