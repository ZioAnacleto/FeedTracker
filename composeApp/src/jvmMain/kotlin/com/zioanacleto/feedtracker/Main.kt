package com.zioanacleto.feedtracker

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.zioanacleto.feedtracker.di.initKoin
import com.zioanacleto.feedtracker.theme.FeedTrackerTheme
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionStore
import com.zioanacleto.feedtracker.widget.NewTrackingNavigator
import com.zioanacleto.feedtracker.widget.TrackingSessionWidgetContent
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.compose_multiplatform
import feedtracker.composeapp.generated.resources.tray_new_tracking_session
import feedtracker.composeapp.generated.resources.tray_quit
import feedtracker.composeapp.generated.resources.tray_show_timer_widget
import feedtracker.composeapp.generated.resources.widget_window_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform

fun main() {
    initKoin {
        printLogger()
    }
    val store = KoinPlatform.getKoin().get<ActiveTrackingSessionStore>()
    application {
        var showWidget by remember { mutableStateOf(false) }
        val trayIcon = painterResource(Res.drawable.compose_multiplatform)
        val openRequested by NewTrackingNavigator.openRequested.collectAsState()
        val newTrackingLabel = stringResource(Res.string.tray_new_tracking_session)
        val showWidgetLabel = stringResource(Res.string.tray_show_timer_widget)
        val quitLabel = stringResource(Res.string.tray_quit)
        val widgetTitle = stringResource(Res.string.widget_window_title)

        Tray(
            icon = trayIcon,
            tooltip = "FeedTracker",
            onAction = { NewTrackingNavigator.requestOpen() },
            menu = {
                Item(
                    text = newTrackingLabel,
                    onClick = { NewTrackingNavigator.requestOpen() },
                )
                Item(
                    text = showWidgetLabel,
                    onClick = { showWidget = true },
                )
                Separator()
                Item(
                    text = quitLabel,
                    onClick = ::exitApplication,
                )
            },
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "FeedTracker",
        ) {
            val awtWindow = this.window
            LaunchedEffect(openRequested) {
                if (openRequested) {
                    awtWindow.toFront()
                    awtWindow.requestFocus()
                }
            }
            App(
                modifier = Modifier.padding(20.dp),
            )
        }

        if (showWidget) {
            val widgetState = rememberWindowState(
                width = 240.dp,
                height = 140.dp,
                position = WindowPosition(Alignment.TopEnd),
            )
            Window(
                onCloseRequest = { showWidget = false },
                title = widgetTitle,
                state = widgetState,
                alwaysOnTop = true,
                resizable = false,
            ) {
                val startTimeMillis by store.startTimeMillis.collectAsState()
                val person by store.person.collectAsState()
                FeedTrackerTheme {
                    TrackingSessionWidgetContent(
                        startTimeMillis = startTimeMillis,
                        displayName = person.displayName,
                        onClick = { NewTrackingNavigator.requestOpen() },
                    )
                }
            }
        }
    }
}
