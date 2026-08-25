package com.zioanacleto.feedtracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zioanacleto.feedtracker.theme.FeedTrackerTheme
import org.koin.compose.KoinContext

@Composable
fun App(modifier: Modifier = Modifier) {
    FeedTrackerTheme {
        FeedTrackerNavHost(modifier)
    }
}
