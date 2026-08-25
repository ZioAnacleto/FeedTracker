package com.zioanacleto.feedtracker.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.zioanacleto.feedtracker.components.TitleWithName
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.add
import feedtracker.composeapp.generated.resources.born_date
import feedtracker.composeapp.generated.resources.duration
import feedtracker.composeapp.generated.resources.duration_format
import feedtracker.composeapp.generated.resources.no_tracking_sessions_yet
import feedtracker.composeapp.generated.resources.retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onNewTrackingClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.loadSessions()
        }
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .then(modifier),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TitleWithName(
                modifier = Modifier.fillMaxWidth(),
                name = "Costanza",
            )
            Spacer(modifier = Modifier.height(16.dp))
            when (val state = uiState) {
                HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = viewModel::loadSessions) {
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }

                is HomeUiState.Ready -> {
                    if (state.sessions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.no_tracking_sessions_yet),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.sessions, key = { it.id }) { session ->
                                TrackingSessionCard(session)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .padding(end = 10.dp, bottom = 20.dp)
                .align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = onNewTrackingClick,
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Filled.Add),
                contentDescription = stringResource(Res.string.add),
                tint = MaterialTheme.colorScheme.background,
            )
        }
    }
}

@Composable
private fun TrackingSessionCard(session: TrackingSessionModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${session.name} ${session.surname}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.born_date, session.birthDate),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    Res.string.duration,
                    formatDuration(session.sessionStartTime, session.sessionEndTime),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            session.additionalNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun formatDuration(startTime: Long, endTime: Long): String {
    val totalSeconds = ((endTime - startTime).coerceAtLeast(0L)) / 1000
    val minutes = (totalSeconds / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()
    return stringResource(Res.string.duration_format, minutes, seconds)
}
