package com.zioanacleto.feedtracker

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.zioanacleto.feedtracker.domain.preferences.isDarkTheme
import com.zioanacleto.feedtracker.domain.repositories.AppearancePreferencesRepository
import com.zioanacleto.feedtracker.widget.FeedTrackerDeepLinks
import com.zioanacleto.feedtracker.widget.NewTrackingNavigator
import org.koin.mp.KoinPlatform

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val systemBars = feedTrackerSystemBarStyle()
        enableEdgeToEdge(
            statusBarStyle = systemBars,
            navigationBarStyle = systemBars,
        )
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun feedTrackerSystemBarStyle(): SystemBarStyle {
        val mode = KoinPlatform.getKoin().get<AppearancePreferencesRepository>().mode.value
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val systemDark = nightMode == Configuration.UI_MODE_NIGHT_YES
        return if (mode.isDarkTheme(systemDark)) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data?.toString()
        if (FeedTrackerDeepLinks.isNewTracking(uri)) {
            NewTrackingNavigator.requestOpen()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
