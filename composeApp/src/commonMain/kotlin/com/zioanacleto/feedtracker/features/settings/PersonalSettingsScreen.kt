package com.zioanacleto.feedtracker.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.components.hideKeyboardOnTouch
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import com.zioanacleto.feedtracker.theme.feedTrackerTextFieldColors
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.cancel
import feedtracker.composeapp.generated.resources.email
import feedtracker.composeapp.generated.resources.log_out
import feedtracker.composeapp.generated.resources.logging_out
import feedtracker.composeapp.generated.resources.logout_confirmation
import feedtracker.composeapp.generated.resources.name
import feedtracker.composeapp.generated.resources.name_placeholder
import feedtracker.composeapp.generated.resources.personal_settings
import feedtracker.composeapp.generated.resources.save_profile
import feedtracker.composeapp.generated.resources.saving_profile
import feedtracker.composeapp.generated.resources.settings_placeholder_account
import feedtracker.composeapp.generated.resources.settings_placeholder_coming_soon
import feedtracker.composeapp.generated.resources.settings_placeholder_language
import feedtracker.composeapp.generated.resources.settings_placeholder_notifications
import feedtracker.composeapp.generated.resources.settings_placeholder_privacy
import feedtracker.composeapp.generated.resources.settings_placeholder_profile
import feedtracker.composeapp.generated.resources.surname
import feedtracker.composeapp.generated.resources.surname_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PersonalSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: PersonalSettingsViewModel = koinViewModel(),
    onBackButtonClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val loggingOutDescription = stringResource(Res.string.logging_out)
    val savingDescription = stringResource(Res.string.saving_profile)
    val focusManager = LocalFocusManager.current
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    val isBusy = uiState.isLoggingOut || uiState.isSaving

    Box(
        modifier = modifier
            .feedTrackerScreenWindowInsets()
            .fillMaxSize()
            .hideKeyboardOnTouch(),
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
                    enabled = !isBusy,
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
                Text(
                    text = stringResource(Res.string.settings_placeholder_profile),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    label = { Text(stringResource(Res.string.email)) },
                    singleLine = true,
                    colors = feedTrackerTextFieldColors(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    label = { Text(stringResource(Res.string.name)) },
                    placeholder = { Text(stringResource(Res.string.name_placeholder)) },
                    singleLine = true,
                    colors = feedTrackerTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    label = { Text(stringResource(Res.string.surname)) },
                    placeholder = { Text(stringResource(Res.string.surname_placeholder)) },
                    singleLine = true,
                    colors = feedTrackerTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.saveProfile()
                        },
                    ),
                )
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.saveProfile()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy && uiState.firstName.isNotBlank() && uiState.lastName.isNotBlank(),
                ) {
                    Text(stringResource(Res.string.save_profile))
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
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
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text(stringResource(Res.string.log_out))
            }
        }

        if (showLogoutConfirmation && !isBusy) {
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

        if (isBusy) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .semantics {
                        contentDescription = if (uiState.isLoggingOut) loggingOutDescription else savingDescription
                    },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        }
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
