package com.zioanacleto.feedtracker.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.zioanacleto.feedtracker.components.TitleWithName
import com.zioanacleto.feedtracker.components.formatSessionDateTime
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.add
import feedtracker.composeapp.generated.resources.born_date
import feedtracker.composeapp.generated.resources.close
import feedtracker.composeapp.generated.resources.collapse_person_sessions
import feedtracker.composeapp.generated.resources.delete_session
import feedtracker.composeapp.generated.resources.duration
import feedtracker.composeapp.generated.resources.duration_format
import feedtracker.composeapp.generated.resources.expand_person_sessions
import feedtracker.composeapp.generated.resources.no_tracking_sessions_yet
import feedtracker.composeapp.generated.resources.notes
import feedtracker.composeapp.generated.resources.retry
import feedtracker.composeapp.generated.resources.session_count
import feedtracker.composeapp.generated.resources.session_date
import feedtracker.composeapp.generated.resources.session_deleted
import feedtracker.composeapp.generated.resources.session_details
import feedtracker.composeapp.generated.resources.undo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: HomeViewModel = koinViewModel(), onNewTrackingClick: () -> Unit) {
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
                    if (state.items.isEmpty()) {
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
                            items(
                                items = state.items,
                                key = { item ->
                                    when (item) {
                                        is HomeSessionListItem.Single -> item.session.id
                                        is HomeSessionListItem.Group -> "group-${item.key}"
                                    }
                                },
                            ) { item ->
                                when (item) {
                                    is HomeSessionListItem.Single -> TrackingSessionCard(
                                        session = item.session,
                                        onClick = { viewModel.selectSession(item.session) },
                                    )
                                    is HomeSessionListItem.Group -> TrackingSessionGroupCard(
                                        group = item,
                                        onToggle = { viewModel.toggleGroup(item.key) },
                                        onSessionClick = viewModel::selectSession,
                                    )
                                }
                            }
                        }
                    }

                    state.selectedSession?.let { session ->
                        SessionDetailDialog(
                            session = session,
                            isDeleting = state.isDeleting,
                            deleteError = state.deleteError,
                            onDismiss = viewModel::dismissSessionDetail,
                            onDelete = viewModel::deleteSelectedSession,
                        )
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

        AnimatedVisibility(
            visible = (uiState as? HomeUiState.Ready)?.undoableDeletedSession != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 88.dp),
            enter = fadeIn(animationSpec = tween(SNACKBAR_FADE_MS)),
            exit = fadeOut(animationSpec = tween(SNACKBAR_FADE_MS)),
        ) {
            DeletedSessionSnackbar(onUndo = viewModel::undoDelete)
        }
    }
}

private const val SNACKBAR_FADE_MS = 400

@Composable
private fun DeletedSessionSnackbar(onUndo: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = HomeViewModel.DELETE_UNDO_WINDOW_MS.toInt(),
                easing = FastOutSlowInEasing,
            ),
        ) { value, _ ->
            progress = value
        }
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 6.dp,
        tonalElevation = 6.dp,
    ) {
        Box {
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.14f),
                        ),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.session_deleted),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = onUndo) {
                    Text(
                        text = stringResource(Res.string.undo),
                        color = MaterialTheme.colorScheme.inversePrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionDetailDialog(
    session: TrackingSessionModel,
    isDeleting: Boolean,
    deleteError: String?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        },
        title = { Text(stringResource(Res.string.session_details)) },
        text = {
            Column {
                Text(
                    text = "${session.name} ${session.surname}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(Res.string.born_date, session.birthDate))
                Text(
                    stringResource(
                        Res.string.session_date,
                        formatSessionDateTime(session.sessionStartTime),
                    ),
                )
                Text(
                    stringResource(
                        Res.string.duration,
                        formatDuration(session.sessionStartTime, session.sessionEndTime),
                    ),
                )
                session.additionalNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.notes),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(text = notes)
                }
                deleteError?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onDelete,
            ) {
                Text(
                    text = stringResource(Res.string.delete_session),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onDismiss,
            ) {
                Text(stringResource(Res.string.close))
            }
        },
    )
}

@Composable
private fun TrackingSessionGroupCard(
    group: HomeSessionListItem.Group,
    onToggle: () -> Unit,
    onSessionClick: (TrackingSessionModel) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${group.name} ${group.surname}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(Res.string.born_date, group.birthDate),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(Res.string.session_count, group.sessions.size),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Icon(
                    painter = rememberVectorPainter(
                        if (group.isExpanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                    ),
                    contentDescription = stringResource(
                        if (group.isExpanded) {
                            Res.string.collapse_person_sessions
                        } else {
                            Res.string.expand_person_sessions
                        },
                    ),
                )
            }
            if (group.isExpanded) {
                group.sessions.forEach { session ->
                    HorizontalDivider()
                    TrackingSessionDetails(
                        session = session,
                        modifier = Modifier
                            .clickable { onSessionClick(session) }
                            .padding(16.dp),
                        showIdentity = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingSessionCard(session: TrackingSessionModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
    ) {
        TrackingSessionDetails(
            session = session,
            modifier = Modifier.padding(16.dp),
            showIdentity = true,
        )
    }
}

@Composable
private fun TrackingSessionDetails(
    session: TrackingSessionModel,
    modifier: Modifier = Modifier,
    showIdentity: Boolean,
) {
    Column(modifier = modifier) {
        if (showIdentity) {
            Text(
                text = "${session.name} ${session.surname}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.born_date, session.birthDate),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
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

@Composable
private fun formatDuration(startTime: Long, endTime: Long): String {
    val totalSeconds = ((endTime - startTime).coerceAtLeast(0L)) / 1000
    val minutes = (totalSeconds / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()
    return stringResource(Res.string.duration_format, minutes, seconds)
}
