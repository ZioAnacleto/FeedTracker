package com.zioanacleto.feedtracker.features.settings.personal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.cancel
import feedtracker.composeapp.generated.resources.log_out
import feedtracker.composeapp.generated.resources.logging_out
import feedtracker.composeapp.generated.resources.logout_confirmation
import feedtracker.composeapp.generated.resources.personal_settings
import feedtracker.composeapp.generated.resources.profile_saved
import feedtracker.composeapp.generated.resources.settings_placeholder_account
import feedtracker.composeapp.generated.resources.settings_placeholder_coming_soon
import feedtracker.composeapp.generated.resources.settings_placeholder_language
import feedtracker.composeapp.generated.resources.settings_placeholder_notifications
import feedtracker.composeapp.generated.resources.settings_placeholder_privacy
import feedtracker.composeapp.generated.resources.settings_placeholder_profile
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PersonalSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: PersonalSettingsViewModel = koinViewModel(),
    onBackButtonClick: () -> Unit,
    onProfileClick: () -> Unit,
    showProfileSavedMessage: Boolean = false,
    onProfileSavedMessageShown: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val loggingOutDescription = stringResource(Res.string.logging_out)
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(showProfileSavedMessage) {
        if (!showProfileSavedMessage) return@LaunchedEffect
        onProfileSavedMessageShown()
        showSavedMessage = true
        delay(PROFILE_SAVED_MESSAGE_MS)
        showSavedMessage = false
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
                    enabled = !uiState.isLoggingOut,
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                        contentDescription = stringResource(Res.string.back),
                    )
                }
                Text(
                    text = stringResource(Res.string.personal_settings),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = ScreenHorizontalPadding),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsMenuRow(
                    title = stringResource(Res.string.settings_placeholder_profile),
                    onClick = onProfileClick,
                    enabled = !uiState.isLoggingOut,
                )
                SettingsPlaceholderRow(title = stringResource(Res.string.settings_placeholder_account))
                SettingsPlaceholderRow(title = stringResource(Res.string.settings_placeholder_notifications))
                SettingsPlaceholderRow(title = stringResource(Res.string.settings_placeholder_privacy))
                SettingsPlaceholderRow(title = stringResource(Res.string.settings_placeholder_language), showDivider = false)
            }

            Button(
                onClick = { showLogoutConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding, vertical = 24.dp),
                enabled = !uiState.isLoggingOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text(stringResource(Res.string.log_out))
            }
        }

        if (showLogoutConfirmation && !uiState.isLoggingOut) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = { Text(stringResource(Res.string.log_out)) },
                text = { Text(stringResource(Res.string.logout_confirmation)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmation = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Text(stringResource(Res.string.log_out))
                    }
                },
                dismissButton = {
                    Button(onClick = { showLogoutConfirmation = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            )
        }

        AnimatedVisibility(
            visible = showSavedMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = ScreenHorizontalPadding, end = ScreenHorizontalPadding, bottom = 96.dp),
            enter = fadeIn(animationSpec = tween(SNACKBAR_FADE_MS)),
            exit = fadeOut(animationSpec = tween(SNACKBAR_FADE_MS)),
        ) {
            ProfileSavedSnackbar()
        }

        if (uiState.isLoggingOut) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .semantics { contentDescription = loggingOutDescription },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun SettingsMenuRow(title: String, onClick: () -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Icon(
            painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.KeyboardArrowRight),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
}

@Composable
private fun SettingsPlaceholderRow(title: String, showDivider: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.settings_placeholder_coming_soon),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
    if (showDivider) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
    }
}

@Composable
private fun ProfileSavedSnackbar() {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 6.dp,
        tonalElevation = 6.dp,
    ) {
        Text(
            text = stringResource(Res.string.profile_saved),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private const val SNACKBAR_FADE_MS = 400
private const val PROFILE_SAVED_MESSAGE_MS = 2_000L
