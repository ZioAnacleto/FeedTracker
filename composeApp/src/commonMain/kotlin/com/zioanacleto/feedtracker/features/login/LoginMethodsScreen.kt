package com.zioanacleto.feedtracker.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.auth_method_apple
import feedtracker.composeapp.generated.resources.auth_method_email
import feedtracker.composeapp.generated.resources.auth_method_google
import feedtracker.composeapp.generated.resources.login_choose_method
import feedtracker.composeapp.generated.resources.login_or_sign_up
import feedtracker.composeapp.generated.resources.or_log_in
import feedtracker.composeapp.generated.resources.retry
import feedtracker.composeapp.generated.resources.sign_up
import feedtracker.composeapp.generated.resources.social_login_not_available
import feedtracker.composeapp.generated.resources.unable_to_load_auth_methods
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginMethodsScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginMethodsViewModel = koinViewModel(),
    onSignUpClick: () -> Unit,
    onEmailMethodSelected: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .feedTrackerScreenWindowInsets()
            .fillMaxSize()
            .padding(horizontal = ScreenHorizontalPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.login_or_sign_up),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.login_choose_method),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSignUpClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.sign_up))
            }
            Spacer(modifier = Modifier.height(24.dp))
            LoginMethodsDivider()
            Spacer(modifier = Modifier.height(24.dp))
            when (val state = uiState) {
                LoginMethodsUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
                }

                is LoginMethodsUiState.Error -> {
                    Text(
                        text = state.message.ifBlank { stringResource(Res.string.unable_to_load_auth_methods) },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = viewModel::loadMethods) {
                        Text(stringResource(Res.string.retry))
                    }
                }

                is LoginMethodsUiState.Ready -> {
                    state.methods.forEach { method ->
                        AuthMethodButton(
                            method = method,
                            onClick = {
                                if (method == AuthMethod.EMAIL) {
                                    onEmailMethodSelected()
                                } else {
                                    viewModel.onUnsupportedMethod()
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (state.showUnsupportedMethod) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.social_login_not_available),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginMethodsDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.or_log_in),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AuthMethodButton(method: AuthMethod, onClick: () -> Unit) {
    val label = when (method) {
        AuthMethod.EMAIL -> stringResource(Res.string.auth_method_email)
        AuthMethod.GOOGLE -> stringResource(Res.string.auth_method_google)
        AuthMethod.APPLE -> stringResource(Res.string.auth_method_apple)
    }
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label)
    }
}
