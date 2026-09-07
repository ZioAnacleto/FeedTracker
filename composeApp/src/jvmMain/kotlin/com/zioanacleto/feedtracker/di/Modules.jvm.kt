package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.JvmNetworkMonitor
import com.zioanacleto.feedtracker.data.local.AUTH_SESSION_FILE_NAME
import com.zioanacleto.feedtracker.data.local.AuthSessionStore
import com.zioanacleto.feedtracker.data.local.FileTextStore
import com.zioanacleto.feedtracker.data.local.JsonAuthSessionStore
import com.zioanacleto.feedtracker.data.local.JsonPendingSessionsStore
import com.zioanacleto.feedtracker.data.local.PENDING_TRACKING_SESSIONS_FILE_NAME
import com.zioanacleto.feedtracker.data.local.PendingSessionsStore
import com.zioanacleto.feedtracker.network.NetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single<NetworkMonitor> { JvmNetworkMonitor(get()) }
    single<PendingSessionsStore> {
        val directory = File(System.getProperty("user.home"), ".feedtracker")
        JsonPendingSessionsStore(
            FileTextStore(File(directory, PENDING_TRACKING_SESSIONS_FILE_NAME).absolutePath),
        )
    }
    single<AuthSessionStore> {
        val directory = File(System.getProperty("user.home"), ".feedtracker")
        JsonAuthSessionStore(
            FileTextStore(File(directory, AUTH_SESSION_FILE_NAME).absolutePath),
        )
    }
}
