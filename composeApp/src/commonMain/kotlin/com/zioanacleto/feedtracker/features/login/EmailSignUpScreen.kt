package com.zioanacleto.feedtracker.features.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.components.hideKeyboardOnTouch
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import com.zioanacleto.feedtracker.theme.feedTrackerTextFieldColors
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.create_account
import feedtracker.composeapp.generated.resources.create_account_hint
import feedtracker.composeapp.generated.resources.email
import feedtracker.composeapp.generated.resources.email_placeholder
import feedtracker.composeapp.generated.resources.enter_verification_code
import feedtracker.composeapp.generated.resources.name
import feedtracker.composeapp.generated.resources.name_placeholder
import feedtracker.composeapp.generated.resources.password
import feedtracker.composeapp.generated.resources.password_placeholder
import feedtracker.composeapp.generated.resources.resend_verification_code
import feedtracker.composeapp.generated.resources.send_verification_code
import feedtracker.composeapp.generated.resources.sign_up
import feedtracker.composeapp.generated.resources.sign_up_busy
import feedtracker.composeapp.generated.resources.sign_up_email_hint
import feedtracker.composeapp.generated.resources.surname
import feedtracker.composeapp.generated.resources.surname_placeholder
import feedtracker.composeapp.generated.resources.verification_code
import feedtracker.composeapp.generated.resources.verification_code_placeholder
import feedtracker.composeapp.generated.resources.verification_email_sent
import feedtracker.composeapp.generated.resources.verify_code
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EmailSignUpScreen(modifier: Modifier = Modifier, viewModel: EmailSignUpViewModel = koinViewModel(), onBackButtonClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val busyDescription = stringResource(Res.string.sign_up_busy)
    val title = when (uiState.step) {
        EmailSignUpStep.Email -> stringResource(Res.string.sign_up)
        EmailSignUpStep.Code -> stringResource(Res.string.enter_verification_code)
        EmailSignUpStep.Profile -> stringResource(Res.string.create_account)
    }

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
                    onClick = {
                        if (!viewModel.goBack()) {
                            onBackButtonClick()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart),
                    enabled = !uiState.isSubmitting,
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                        contentDescription = stringResource(Res.string.back),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding),
            ) {
                when (uiState.step) {
                    EmailSignUpStep.Email -> EmailStep(
                        uiState = uiState,
                        onEmailChange = viewModel::onEmailChange,
                        onSubmit = {
                            focusManager.clearFocus()
                            viewModel.sendVerificationCode()
                        },
                    )

                    EmailSignUpStep.Code -> CodeStep(
                        uiState = uiState,
                        onCodeChange = viewModel::onCodeChange,
                        onVerify = {
                            focusManager.clearFocus()
                            viewModel.verifyCode()
                        },
                        onResend = {
                            focusManager.clearFocus()
                            viewModel.sendVerificationCode()
                        },
                    )

                    EmailSignUpStep.Profile -> ProfileStep(
                        uiState = uiState,
                        onFirstNameChange = viewModel::onFirstNameChange,
                        onLastNameChange = viewModel::onLastNameChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onSubmit = {
                            focusManager.clearFocus()
                            viewModel.completeRegistration()
                        },
                    )
                }
            }
        }

        if (uiState.isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .semantics { contentDescription = busyDescription },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun EmailStep(uiState: EmailSignUpUiState, onEmailChange: (String) -> Unit, onSubmit: () -> Unit) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(Res.string.sign_up_email_hint),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = uiState.email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting,
        label = { Text(stringResource(Res.string.email)) },
        placeholder = { Text(stringResource(Res.string.email_placeholder)) },
        singleLine = true,
        colors = feedTrackerTextFieldColors(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
    )
    SignUpError(uiState.error)
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting && uiState.email.isNotBlank(),
    ) {
        Text(stringResource(Res.string.send_verification_code))
    }
}

@Composable
private fun CodeStep(uiState: EmailSignUpUiState, onCodeChange: (String) -> Unit, onVerify: () -> Unit, onResend: () -> Unit) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(Res.string.verification_email_sent),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = uiState.code,
        onValueChange = onCodeChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting,
        label = { Text(stringResource(Res.string.verification_code)) },
        placeholder = { Text(stringResource(Res.string.verification_code_placeholder)) },
        singleLine = true,
        colors = feedTrackerTextFieldColors(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onVerify() }),
    )
    SignUpError(uiState.error)
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onVerify,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting && uiState.code.length == EmailSignUpViewModel.CODE_LENGTH,
    ) {
        Text(stringResource(Res.string.verify_code))
    }
    TextButton(
        onClick = onResend,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting,
    ) {
        Text(stringResource(Res.string.resend_verification_code))
    }
}

@Composable
private fun ProfileStep(
    uiState: EmailSignUpUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(Res.string.create_account_hint),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = uiState.firstName,
        onValueChange = onFirstNameChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting,
        label = { Text(stringResource(Res.string.name)) },
        placeholder = { Text(stringResource(Res.string.name_placeholder)) },
        singleLine = true,
        colors = feedTrackerTextFieldColors(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = uiState.lastName,
        onValueChange = onLastNameChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting,
        label = { Text(stringResource(Res.string.surname)) },
        placeholder = { Text(stringResource(Res.string.surname_placeholder)) },
        singleLine = true,
        colors = feedTrackerTextFieldColors(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting,
        label = { Text(stringResource(Res.string.password)) },
        placeholder = { Text(stringResource(Res.string.password_placeholder)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        colors = feedTrackerTextFieldColors(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
    )
    SignUpError(uiState.error)
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isSubmitting &&
            uiState.firstName.isNotBlank() &&
            uiState.lastName.isNotBlank() &&
            uiState.password.isNotBlank(),
    ) {
        Text(stringResource(Res.string.create_account))
    }
}

@Composable
private fun SignUpError(error: String?) {
    error ?: return
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = error,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}
