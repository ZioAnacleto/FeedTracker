package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.ConnectivityManagerNetworkMonitor
import com.zioanacleto.feedtracker.data.local.AUTH_SESSION_FILE_NAME
import com.zioanacleto.feedtracker.data.local.AuthSessionStore
import com.zioanacleto.feedtracker.data.local.FileTextStore
import com.zioanacleto.feedtracker.data.local.JsonAuthSessionStore
import com.zioanacleto.feedtracker.data.local.JsonPendingSessionsStore
import com.zioanacleto.feedtracker.data.local.PENDING_TRACKING_SESSIONS_FILE_NAME
import com.zioanacleto.feedtracker.data.local.PendingSessionsStore
import com.zioanacleto.feedtracker.network.NetworkMonitor
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionNotifier
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionStore
import com.zioanacleto.feedtracker.widget.AndroidActiveTrackingSessionNotifier
import com.zioanacleto.feedtracker.widget.AndroidActiveTrackingSessionStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single<NetworkMonitor> { ConnectivityManagerNetworkMonitor(androidContext(), get()) }
    single<ActiveTrackingSessionStore> { AndroidActiveTrackingSessionStore(androidContext()) }
    single<ActiveTrackingSessionNotifier> { AndroidActiveTrackingSessionNotifier(androidContext()) }
    single<PendingSessionsStore> {
        JsonPendingSessionsStore(
            FileTextStore(File(androidContext().filesDir, PENDING_TRACKING_SESSIONS_FILE_NAME).absolutePath),
        )
    }
    single<AuthSessionStore> {
        JsonAuthSessionStore(
            FileTextStore(File(androidContext().filesDir, AUTH_SESSION_FILE_NAME).absolutePath),
        )
    }
}
