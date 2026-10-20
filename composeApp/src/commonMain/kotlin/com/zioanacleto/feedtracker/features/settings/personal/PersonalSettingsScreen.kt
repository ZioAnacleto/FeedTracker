package com.zioanacleto.feedtracker.features.settings.personal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.components.MessageSnackbar
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.appearance
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.language
import feedtracker.composeapp.generated.resources.personal_settings
import feedtracker.composeapp.generated.resources.privacy
import feedtracker.composeapp.generated.resources.profile_saved
import feedtracker.composeapp.generated.resources.settings_placeholder_account
import feedtracker.composeapp.generated.resources.settings_placeholder_coming_soon
import feedtracker.composeapp.generated.resources.settings_placeholder_notifications
import feedtracker.composeapp.generated.resources.settings_placeholder_profile
import feedtracker.composeapp.generated.resources.tracking_preferences
import feedtracker.composeapp.generated.resources.tracking_preferences_saved
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun PersonalSettingsScreen(
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAccountClick: () -> Unit,
    onTrackingPreferencesClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    showProfileSavedMessage: Boolean = false,
    onProfileSavedMessageShown: () -> Unit = {},
    showTrackingPreferencesSavedMessage: Boolean = false,
    onTrackingPreferencesSavedMessageShown: () -> Unit = {},
) {
    var showSavedMessage by remember { mutableStateOf(false) }
    var savedMessage by remember { mutableStateOf("") }
    val profileSavedText = stringResource(Res.string.profile_saved)
    val trackingPreferencesSavedText = stringResource(Res.string.tracking_preferences_saved)

    LaunchedEffect(showProfileSavedMessage) {
        if (!showProfileSavedMessage) return@LaunchedEffect
        onProfileSavedMessageShown()
        savedMessage = profileSavedText
        showSavedMessage = true
        delay(PROFILE_SAVED_MESSAGE_MS)
        showSavedMessage = false
    }

    LaunchedEffect(showTrackingPreferencesSavedMessage) {
        if (!showTrackingPreferencesSavedMessage) return@LaunchedEffect
        onTrackingPreferencesSavedMessageShown()
        savedMessage = trackingPreferencesSavedText
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenHorizontalPadding),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsMenuRow(
                    title = stringResource(Res.string.settings_placeholder_profile),
                    onClick = onProfileClick,
                )
                SettingsMenuRow(
                    title = stringResource(Res.string.tracking_preferences),
                    onClick = onTrackingPreferencesClick,
                )
                SettingsMenuRow(
                    title = stringResource(Res.string.appearance),
                    onClick = onAppearanceClick,
                )
                SettingsMenuRow(
                    title = stringResource(Res.string.settings_placeholder_account),
                    onClick = onAccountClick,
                )
                SettingsPlaceholderRow(title = stringResource(Res.string.settings_placeholder_notifications))
                SettingsMenuRow(
                    title = stringResource(Res.string.privacy),
                    onClick = onPrivacyClick,
                )
                SettingsMenuRow(
                    title = stringResource(Res.string.language),
                    onClick = onLanguageClick,
                    showDivider = false,
                )
            }
        }

        AnimatedVisibility(
            visible = showSavedMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = ScreenHorizontalPadding, end = ScreenHorizontalPadding, bottom = 96.dp),
            enter = fadeIn(animationSpec = tween(SNACKBAR_FADE_MS)),
            exit = fadeOut(animationSpec = tween(SNACKBAR_FADE_MS)),
        ) {
            MessageSnackbar(message = savedMessage)
        }
    }
}

@Composable
private fun SettingsMenuRow(title: String, onClick: () -> Unit, showDivider: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
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
    if (showDivider) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
    }
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

private const val SNACKBAR_FADE_MS = 400
private const val PROFILE_SAVED_MESSAGE_MS = 2_000L
