package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.ConnectivityManagerNetworkMonitor
import com.zioanacleto.feedtracker.network.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<NetworkMonitor> { ConnectivityManagerNetworkMonitor(androidContext(), get()) }
}