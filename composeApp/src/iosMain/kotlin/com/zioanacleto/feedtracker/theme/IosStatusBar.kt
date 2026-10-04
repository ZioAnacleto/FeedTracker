package com.zioanacleto.feedtracker.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

private var iosStatusBarListener: (Boolean, Boolean) -> Unit = { _, _ -> }

fun setIosStatusBarStyle(listener: (Boolean, Boolean) -> Unit) {
    iosStatusBarListener = listener
}

@Composable
actual fun ApplyFeedTrackerSystemBars(darkTheme: Boolean, followSystem: Boolean) {
    SideEffect {
        iosStatusBarListener(followSystem, darkTheme)
    }
}
