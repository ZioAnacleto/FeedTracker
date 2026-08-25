package com.zioanacleto.feedtracker.features.newtracking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.components.AnimatedTimer
import com.zioanacleto.feedtracker.components.hideKeyboardOnTouch
import com.zioanacleto.feedtracker.getCurrentTimeMillis
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.cancel
import feedtracker.composeapp.generated.resources.clear_date_of_birth
import feedtracker.composeapp.generated.resources.clear_name
import feedtracker.composeapp.generated.resources.clear_surname
import feedtracker.composeapp.generated.resources.date_of_birth
import feedtracker.composeapp.generated.resources.date_of_birth_placeholder
import feedtracker.composeapp.generated.resources.name
import feedtracker.composeapp.generated.resources.name_placeholder
import feedtracker.composeapp.generated.resources.new_tracking_session
import feedtracker.composeapp.generated.resources.notes
import feedtracker.composeapp.generated.resources.notes_placeholder
import feedtracker.composeapp.generated.resources.ok
import feedtracker.composeapp.generated.resources.save_new_tracking
import feedtracker.composeapp.generated.resources.save_tracking_confirmation
import feedtracker.composeapp.generated.resources.surname
import feedtracker.composeapp.generated.resources.surname_placeholder
import feedtracker.composeapp.generated.resources.unable_to_save
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewTrackingScreen(modifier: Modifier = Modifier, onBackButtonClick: () -> Unit) {
    var nameTextField by remember { mutableStateOf(TextFieldValue("")) }
    var surnameTextField by remember { mutableStateOf(TextFieldValue("")) }
    var birthDateTextField by remember { mutableStateOf(TextFieldValue("")) }
    var additionalNotesTextField by remember { mutableStateOf(TextFieldValue("")) }

    val isButtonEnabled by remember {
        derivedStateOf {
            nameTextField.text.isNotEmpty() &&
                surnameTextField.text.isNotEmpty() &&
                (birthDateTextField.text.isNotEmpty() && birthDateTextField.text.length == 10)
        }
    }

    var hasNameFocus by remember { mutableStateOf(false) }
    var hasSurnameFocus by remember { mutableStateOf(false) }
    var hasBirthDateFocus by remember { mutableStateOf(false) }
    var startTime by remember { mutableLongStateOf(0L) }
    var stopTime by remember { mutableLongStateOf(0L) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(true) }

    val viewModel = koinViewModel<NewTrackingViewModel>()
    val showPopup by viewModel.showPopup.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is SaveTrackingUiState.Saved) {
            viewModel.consumeSaveState()
            onBackButtonClick()
        }
    }

    val localFocusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        startTime = getCurrentTimeMillis()
        while (isTimerRunning) {
            elapsedTime = getCurrentTimeMillis() - startTime
            delay(10)
        }
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .hideKeyboardOnTouch(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.new_tracking_session),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            // Timer Display
            AnimatedTimer(
                time = elapsedTime,
                modifier = Modifier.padding(vertical = 24.dp),
            )

            // Name
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .onFocusChanged {
                        hasNameFocus = it.hasFocus
                    },
                value = nameTextField,
                onValueChange = { nameTextField = it },
                label = { Text(stringResource(Res.string.name)) },
                placeholder = { Text(stringResource(Res.string.name_placeholder)) },
                singleLine = true,
                trailingIcon = {
                    if (hasNameFocus && nameTextField.text.isNotEmpty()) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Clear),
                            contentDescription = stringResource(Res.string.clear_name),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { nameTextField = TextFieldValue("") },
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    showKeyboardOnFocus = true,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(10.dp),
            )

            // Surname
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .onFocusChanged {
                        hasSurnameFocus = it.hasFocus
                    },
                value = surnameTextField,
                onValueChange = { surnameTextField = it },
                label = { Text(stringResource(Res.string.surname)) },
                placeholder = { Text(stringResource(Res.string.surname_placeholder)) },
                singleLine = true,
                trailingIcon = {
                    if (hasSurnameFocus && surnameTextField.text.isNotEmpty()) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Clear),
                            contentDescription = stringResource(Res.string.clear_surname),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { surnameTextField = TextFieldValue("") },
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    showKeyboardOnFocus = true,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(10.dp),
            )

            // Date of birth
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .onFocusChanged {
                        hasBirthDateFocus = it.hasFocus
                    },
                value = birthDateTextField,
                onValueChange = { input ->
                    val oldText = birthDateTextField.text
                    val newText = input.text

                    if (newText.length <= 10) {
                        if (newText.length > oldText.length) {
                            // Adding characters: format with slashes
                            val digits = newText.filter { it.isDigit() }
                            val formatted = buildString {
                                for (i in digits.indices) {
                                    append(digits[i])
                                    if ((i == 1 || i == 3) && i == digits.lastIndex && i < 4) {
                                        append("/")
                                    } else if ((i == 1 || i == 3) && i < digits.lastIndex) {
                                        append("/")
                                    }
                                }
                            }
                            birthDateTextField = input.copy(
                                text = formatted,
                                selection = TextRange(formatted.length),
                            )
                            if (newText.length == 10) localFocusManager.clearFocus()
                        } else {
                            // Deleting characters: allow standard deletion
                            birthDateTextField = input
                        }
                    }
                },
                label = { Text(stringResource(Res.string.date_of_birth)) },
                placeholder = { Text(stringResource(Res.string.date_of_birth_placeholder)) },
                singleLine = true,
                trailingIcon = {
                    if (hasBirthDateFocus && birthDateTextField.text.isNotEmpty()) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Clear),
                            contentDescription = stringResource(Res.string.clear_date_of_birth),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { birthDateTextField = TextFieldValue("") },
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    showKeyboardOnFocus = true,
                    imeAction = ImeAction.Done,
                ),
                shape = RoundedCornerShape(10.dp),
            )

            // Additional notes
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                value = additionalNotesTextField,
                onValueChange = { additionalNotesTextField = it },
                label = { Text(stringResource(Res.string.notes)) },
                placeholder = { Text(stringResource(Res.string.notes_placeholder)) },
                singleLine = false,
                minLines = 6,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    showKeyboardOnFocus = true,
                    imeAction = ImeAction.Done,
                ),
                shape = RoundedCornerShape(10.dp),
            )
        }

        Button(
            onClick = {
                viewModel.showPopup()
            },
            enabled = isButtonEnabled,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.BottomCenter),
        ) {
            Text(stringResource(Res.string.save_new_tracking))
        }
    }

    if (showPopup) {
        AlertDialog(
            onDismissRequest = { viewModel.hidePopup() },
            title = { Text(stringResource(Res.string.save_new_tracking)) },
            text = { Text(stringResource(Res.string.save_tracking_confirmation)) },
            confirmButton = {
                Button(
                    enabled = saveState !is SaveTrackingUiState.Saving,
                    onClick = {
                        if (isTimerRunning) {
                            isTimerRunning = false
                            stopTime = getCurrentTimeMillis()
                        }

                        viewModel.saveNewTracking(
                            name = nameTextField.text,
                            surname = surnameTextField.text,
                            birthDate = birthDateTextField.text,
                            additionalNotes = additionalNotesTextField.text,
                            startTime = startTime,
                            endTime = stopTime,
                        )
                        viewModel.hidePopup()
                    },
                ) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.hidePopup() }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    val saveError = saveState as? SaveTrackingUiState.Error
    if (saveError != null) {
        AlertDialog(
            onDismissRequest = viewModel::consumeSaveState,
            title = { Text(stringResource(Res.string.unable_to_save)) },
            text = { Text(saveError.message) },
            confirmButton = {
                Button(onClick = viewModel::consumeSaveState) {
                    Text(stringResource(Res.string.ok))
                }
            },
        )
    }
}
