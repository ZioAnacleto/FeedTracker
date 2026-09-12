package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.IosNetworkMonitor
import com.zioanacleto.feedtracker.data.local.AUTH_SESSION_FILE_NAME
import com.zioanacleto.feedtracker.data.local.AuthSessionStore
import com.zioanacleto.feedtracker.data.local.FileTextStore
import com.zioanacleto.feedtracker.data.local.JsonAuthSessionStore
import com.zioanacleto.feedtracker.data.local.JsonPendingSessionsStore
import com.zioanacleto.feedtracker.data.local.JsonTrackingPreferencesStore
import com.zioanacleto.feedtracker.data.local.PENDING_TRACKING_SESSIONS_FILE_NAME
import com.zioanacleto.feedtracker.data.local.PendingSessionsStore
import com.zioanacleto.feedtracker.data.local.TRACKING_PREFERENCES_FILE_NAME
import com.zioanacleto.feedtracker.data.local.TrackingPreferencesStore
import com.zioanacleto.feedtracker.network.NetworkMonitor
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionNotifier
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionStore
import com.zioanacleto.feedtracker.widget.IosActiveTrackingSessionNotifier
import com.zioanacleto.feedtracker.widget.IosActiveTrackingSessionStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<ActiveTrackingSessionStore> { IosActiveTrackingSessionStore() }
    single<ActiveTrackingSessionNotifier> { IosActiveTrackingSessionNotifier() }
    single<PendingSessionsStore> {
        val documentsUrl = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        val path = requireNotNull(
            documentsUrl?.URLByAppendingPathComponent(PENDING_TRACKING_SESSIONS_FILE_NAME)?.path,
        )
        JsonPendingSessionsStore(FileTextStore(path))
    }
    single<AuthSessionStore> {
        val documentsUrl = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        val path = requireNotNull(
            documentsUrl?.URLByAppendingPathComponent(AUTH_SESSION_FILE_NAME)?.path,
        )
        JsonAuthSessionStore(FileTextStore(path))
    }
    single<TrackingPreferencesStore> {
        val documentsUrl = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        val path = requireNotNull(
            documentsUrl?.URLByAppendingPathComponent(TRACKING_PREFERENCES_FILE_NAME)?.path,
        )
        JsonTrackingPreferencesStore(FileTextStore(path))
    }
}
