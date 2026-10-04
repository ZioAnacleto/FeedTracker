package com.zioanacleto.feedtracker.theme

import androidx.compose.runtime.Composable

@Composable
actual fun ApplyFeedTrackerSystemBars(darkTheme: Boolean, followSystem: Boolean) {
    // Desktop windows draw the themed content directly and have no OS status bar.
    if (darkTheme || followSystem) return
}
