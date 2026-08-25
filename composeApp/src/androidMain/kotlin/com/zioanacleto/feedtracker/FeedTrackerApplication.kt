package com.zioanacleto.feedtracker

import android.app.Application
import com.zioanacleto.feedtracker.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class FeedTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@FeedTrackerApplication)
            androidLogger()
        }
    }
}