package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.JvmNetworkMonitor
import com.zioanacleto.feedtracker.network.NetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<NetworkMonitor> { JvmNetworkMonitor(get()) }
}
