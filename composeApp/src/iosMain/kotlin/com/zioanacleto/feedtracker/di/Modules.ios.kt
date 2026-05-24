package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.IosNetworkMonitor
import com.zioanacleto.feedtracker.network.NetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<NetworkMonitor> { IosNetworkMonitor() }
}
