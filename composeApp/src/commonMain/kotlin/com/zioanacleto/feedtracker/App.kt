package com.zioanacleto.feedtracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import com.zioanacleto.feedtracker.theme.FeedTrackerTheme
import org.koin.compose.koinInject

@Composable
fun App(modifier: Modifier = Modifier) {
    FeedTrackerTheme {
        val authSessionRepository = koinInject<AuthSessionRepository>()
        val session by authSessionRepository.session.collectAsState()
        FeedTrackerNavHost(
            isLoggedIn = session != null,
            modifier = modifier,
        )
    }
}
