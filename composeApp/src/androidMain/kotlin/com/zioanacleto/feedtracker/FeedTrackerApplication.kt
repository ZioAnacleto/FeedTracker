package com.zioanacleto.feedtracker

import android.app.Application
import com.zioanacleto.feedtracker.di.initKoin
import com.zioanacleto.feedtracker.domain.repositories.LanguagePreferencesRepository
import com.zioanacleto.feedtracker.locale.applyAndroidAppLanguage
import com.zioanacleto.feedtracker.widget.TrackingSessionNotificationController
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.mp.KoinPlatform

class FeedTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@FeedTrackerApplication)
            androidLogger()
        }
        val language = KoinPlatform.getKoin().get<LanguagePreferencesRepository>().language.value
        applyAndroidAppLanguage(this, language)
        TrackingSessionNotificationController.ensureChannel(this)
    }
}
