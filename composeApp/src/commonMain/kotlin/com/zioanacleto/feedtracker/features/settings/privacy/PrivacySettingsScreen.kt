package com.zioanacleto.feedtracker.features.settings.privacy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.components.MessageSnackbar
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.export_sessions_csv
import feedtracker.composeapp.generated.resources.export_sessions_description
import feedtracker.composeapp.generated.resources.export_sessions_file_csv
import feedtracker.composeapp.generated.resources.export_sessions_file_json
import feedtracker.composeapp.generated.resources.export_sessions_json
import feedtracker.composeapp.generated.resources.export_sessions_saved
import feedtracker.composeapp.generated.resources.export_sessions_title
import feedtracker.composeapp.generated.resources.exporting_sessions
import feedtracker.composeapp.generated.resources.privacy
import feedtracker.composeapp.generated.resources.save_sessions_csv
import feedtracker.composeapp.generated.resources.save_sessions_json
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PrivacySettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: PrivacySettingsViewModel = koinViewModel(),
    onBackButtonClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportingDescription = stringResource(Res.string.exporting_sessions)
    val savedMessage = stringResource(Res.string.export_sessions_saved)

    SessionExportSaveEffect(
        pendingSave = uiState.pendingSave,
        saveLauncherOpen = uiState.saveLauncherOpen,
        onSaveLauncherOpened = viewModel::onSaveLauncherOpened,
        onSaved = viewModel::onSaveCompleted,
        onCancelled = viewModel::onSaveCancelled,
        onFailed = viewModel::onSaveFailed,
        onCompleteSave = viewModel::completePendingSave,
    )

    LaunchedEffect(uiState.saveSucceeded) {
        if (!uiState.saveSucceeded) return@LaunchedEffect
        delay(SAVED_MESSAGE_MS)
        viewModel.onSaveSucceededMessageShown()
    }

    Box(
        modifier = modifier
            .feedTrackerScreenWindowInsets()
            .fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                IconButton(
                    onClick = onBackButtonClick,
                    modifier = Modifier.align(Alignment.CenterStart),
                    enabled = !uiState.isExporting,
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                        contentDescription = stringResource(Res.string.back),
                    )
                }
                Text(
                    text = stringResource(Res.string.privacy),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenHorizontalPadding),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.export_sessions_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.export_sessions_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.exportSessions(
                            SessionExportFormat.CSV,
                            exportDestination(uiState.showsSeparateSaveActions),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isExporting,
                ) {
                    Text(
                        stringResource(
                            if (uiState.showsSeparateSaveActions) {
                                Res.string.export_sessions_csv
                            } else {
                                Res.string.export_sessions_file_csv
                            },
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.exportSessions(
                            SessionExportFormat.JSON,
                            exportDestination(uiState.showsSeparateSaveActions),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isExporting,
                ) {
                    Text(
                        stringResource(
                            if (uiState.showsSeparateSaveActions) {
                                Res.string.export_sessions_json
                            } else {
                                Res.string.export_sessions_file_json
                            },
                        ),
                    )
                }
                if (uiState.showsSeparateSaveActions) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SAVE)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isExporting,
                    ) {
                        Text(stringResource(Res.string.save_sessions_csv))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SAVE)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isExporting,
                    ) {
                        Text(stringResource(Res.string.save_sessions_json))
                    }
                }
                uiState.error?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.saveSucceeded,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = ScreenHorizontalPadding, end = ScreenHorizontalPadding, bottom = 24.dp),
            enter = fadeIn(animationSpec = tween(SNACKBAR_FADE_MS)),
            exit = fadeOut(animationSpec = tween(SNACKBAR_FADE_MS)),
        ) {
            MessageSnackbar(message = savedMessage)
        }

        if (uiState.isExporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .semantics { contentDescription = exportingDescription },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

private const val SNACKBAR_FADE_MS = 400
private const val SAVED_MESSAGE_MS = 2_000L
