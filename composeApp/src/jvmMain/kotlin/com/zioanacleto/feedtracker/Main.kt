package com.zioanacleto.feedtracker

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.zioanacleto.feedtracker.di.initKoin

fun main() = application {
    initKoin {
        printLogger()
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "FeedTracker",
    ) {
        App(
            modifier = Modifier
                .padding(20.dp),
        )
    }
}
