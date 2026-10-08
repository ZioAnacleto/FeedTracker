package com.zioanacleto.feedtracker.features.settings.account

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import com.zioanacleto.feedtracker.theme.feedTrackerTextFieldColors
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.cancel
import feedtracker.composeapp.generated.resources.delete_account
import feedtracker.composeapp.generated.resources.delete_account_confirm
import feedtracker.composeapp.generated.resources.delete_account_confirmation
import feedtracker.composeapp.generated.resources.delete_account_description
import feedtracker.composeapp.generated.resources.delete_account_phrase
import feedtracker.composeapp.generated.resources.delete_account_title
import feedtracker.composeapp.generated.resources.delete_account_type_phrase
import feedtracker.composeapp.generated.resources.deleting_account
import feedtracker.composeapp.generated.resources.log_out
import feedtracker.composeapp.generated.resources.logging_out
import feedtracker.composeapp.generated.resources.logout_confirmation
import feedtracker.composeapp.generated.resources.settings_placeholder_account
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountSettingsViewModel = koinViewModel(),
    onBackButtonClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val loggingOutDescription = stringResource(Res.string.logging_out)
    val deletingDescription = stringResource(Res.string.deleting_account)
    val confirmationPhrase = stringResource(Res.string.delete_account_phrase)
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var confirmationText by remember { mutableStateOf("") }
    val busy = uiState.isLoggingOut || uiState.isDeletingAccount

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
                    enabled = !busy,
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                        contentDescription = stringResource(Res.string.back),
                    )
                }
                Text(
                    text = stringResource(Res.string.settings_placeholder_account),
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
                Button(
                    onClick = { showLogoutConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(stringResource(Res.string.log_out))
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(Res.string.delete_account_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.delete_account_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        confirmationText = ""
                        showDeleteConfirmation = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(stringResource(Res.string.delete_account))
                }
                uiState.error?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
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

        if (showDeleteConfirmation && !uiState.isDeletingAccount) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text(stringResource(Res.string.delete_account)) },
                text = {
                    Column {
                        Text(stringResource(Res.string.delete_account_confirmation, confirmationPhrase))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = confirmationText,
                            onValueChange = { confirmationText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(stringResource(Res.string.delete_account_type_phrase)) },
                            colors = feedTrackerTextFieldColors(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmation = false
                            viewModel.deleteAccount(confirmationText, confirmationPhrase)
                        },
                        enabled = confirmationText.trim() == confirmationPhrase,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Text(stringResource(Res.string.delete_account_confirm))
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteConfirmation = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            )
        }

        if (busy) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .semantics {
                        contentDescription = if (uiState.isDeletingAccount) deletingDescription else loggingOutDescription
                    },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
