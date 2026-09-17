package com.zioanacleto.feedtracker.features.pasttracking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.components.DurationDial
import com.zioanacleto.feedtracker.components.birthDateChange
import com.zioanacleto.feedtracker.components.canonicalBirthDateFromDisplay
import com.zioanacleto.feedtracker.components.civilDateFromUtcEpochMillis
import com.zioanacleto.feedtracker.components.formatBirthDateForDisplay
import com.zioanacleto.feedtracker.components.formatCivilDate
import com.zioanacleto.feedtracker.components.formatClockTime
import com.zioanacleto.feedtracker.components.hideKeyboardOnTouch
import com.zioanacleto.feedtracker.components.isPastSessionInRange
import com.zioanacleto.feedtracker.components.isPastTrackingSaveEnabled
import com.zioanacleto.feedtracker.components.isSelectablePastUtcDate
import com.zioanacleto.feedtracker.components.localDateTimeFromEpochMillis
import com.zioanacleto.feedtracker.components.localDateTimeToEpochMillis
import com.zioanacleto.feedtracker.components.placeholder
import com.zioanacleto.feedtracker.components.startPartsAfterDurationChange
import com.zioanacleto.feedtracker.components.startPartsKeepingSessionInPast
import com.zioanacleto.feedtracker.components.utcEpochMillisFromCivilDate
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.repositories.TrackingPreferencesRepository
import com.zioanacleto.feedtracker.features.newtracking.NewTrackingViewModel
import com.zioanacleto.feedtracker.features.newtracking.SaveTrackingUiState
import com.zioanacleto.feedtracker.getCurrentTimeMillis
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import com.zioanacleto.feedtracker.theme.feedTrackerTextButtonColors
import com.zioanacleto.feedtracker.theme.feedTrackerTextFieldColors
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.cancel
import feedtracker.composeapp.generated.resources.clear_date_of_birth
import feedtracker.composeapp.generated.resources.clear_name
import feedtracker.composeapp.generated.resources.clear_surname
import feedtracker.composeapp.generated.resources.date_of_birth
import feedtracker.composeapp.generated.resources.discard_session_confirmation
import feedtracker.composeapp.generated.resources.discard_session_title
import feedtracker.composeapp.generated.resources.leave
import feedtracker.composeapp.generated.resources.name
import feedtracker.composeapp.generated.resources.name_placeholder
import feedtracker.composeapp.generated.resources.notes
import feedtracker.composeapp.generated.resources.notes_placeholder
import feedtracker.composeapp.generated.resources.ok
import feedtracker.composeapp.generated.resources.past_session_date
import feedtracker.composeapp.generated.resources.past_session_duration
import feedtracker.composeapp.generated.resources.past_session_must_be_in_the_past
import feedtracker.composeapp.generated.resources.past_session_start_time
import feedtracker.composeapp.generated.resources.past_tracking_session
import feedtracker.composeapp.generated.resources.save_new_tracking
import feedtracker.composeapp.generated.resources.save_tracking_confirmation
import feedtracker.composeapp.generated.resources.surname
import feedtracker.composeapp.generated.resources.surname_placeholder
import feedtracker.composeapp.generated.resources.unable_to_save
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val DEFAULT_DURATION_MS = 15L * 60L * 1000L

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PastTrackingScreen(
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialSurname: String = "",
    initialBirthDate: String = "",
    onBackButtonClick: () -> Unit,
) {
    val nowMillis = remember { getCurrentTimeMillis() }
    val initialParts = remember(nowMillis) {
        localDateTimeFromEpochMillis((nowMillis - DEFAULT_DURATION_MS).coerceAtLeast(0L))
    }

    val viewModel = koinViewModel<NewTrackingViewModel>()
    val preferences by koinInject<TrackingPreferencesRepository>().preferences.collectAsState()
    val dateFormat = preferences.dateFormat
    val showPopup by viewModel.showPopup.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val localFocusManager = LocalFocusManager.current

    var nameTextField by remember { mutableStateOf(TextFieldValue(initialName)) }
    var surnameTextField by remember { mutableStateOf(TextFieldValue(initialSurname)) }
    var birthDateTextField by remember(dateFormat, initialBirthDate) {
        mutableStateOf(TextFieldValue(formatBirthDateForDisplay(initialBirthDate, dateFormat)))
    }
    var additionalNotesTextField by remember { mutableStateOf(TextFieldValue("")) }
    var sessionDate by remember { mutableStateOf(initialParts.date) }
    var startHour by remember { mutableIntStateOf(initialParts.hour) }
    var startMinute by remember { mutableIntStateOf(initialParts.minute) }
    var durationMs by remember { mutableLongStateOf(DEFAULT_DURATION_MS) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var hasNameFocus by remember { mutableStateOf(false) }
    var hasSurnameFocus by remember { mutableStateOf(false) }
    var hasBirthDateFocus by remember { mutableStateOf(false) }

    val startMillis = localDateTimeToEpochMillis(sessionDate, startHour, startMinute)
    val nowMillisForValidation = getCurrentTimeMillis()
    val isPastSession = isPastSessionInRange(
        date = sessionDate,
        hour = startHour,
        minute = startMinute,
        durationMs = durationMs,
        nowMillis = nowMillisForValidation,
    )
    val isButtonEnabled = isPastTrackingSaveEnabled(
        name = nameTextField.text,
        surname = surnameTextField.text,
        birthDate = birthDateTextField.text,
        isPastSession = isPastSession,
    )

    LaunchedEffect(saveState) {
        if (saveState is SaveTrackingUiState.Saved) {
            viewModel.consumeSaveState()
            onBackButtonClick()
        }
    }

    BackHandler(enabled = !showDiscardDialog) {
        showDiscardDialog = true
    }

    Box(
        modifier = modifier
            .feedTrackerScreenWindowInsets()
            .fillMaxSize()
            .hideKeyboardOnTouch(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                IconButton(
                    onClick = { showDiscardDialog = true },
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                        contentDescription = stringResource(Res.string.back),
                    )
                }
                Text(
                    text = stringResource(Res.string.past_tracking_session),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 48.dp, vertical = 8.dp),
                )
            }

            Text(
                text = stringResource(Res.string.past_session_duration),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding, vertical = 4.dp),
            )
            DurationDial(
                durationMs = durationMs,
                onDurationChange = { newDuration ->
                    val shifted = startPartsAfterDurationChange(
                        date = sessionDate,
                        hour = startHour,
                        minute = startMinute,
                        previousDurationMs = durationMs,
                        newDurationMs = newDuration,
                        nowMillis = getCurrentTimeMillis(),
                    )
                    sessionDate = shifted.date
                    startHour = shifted.hour
                    startMinute = shifted.minute
                    durationMs = newDuration
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding),
            )

            PersonNameFields(
                nameTextField = nameTextField,
                surnameTextField = surnameTextField,
                birthDateTextField = birthDateTextField,
                hasNameFocus = hasNameFocus,
                hasSurnameFocus = hasSurnameFocus,
                hasBirthDateFocus = hasBirthDateFocus,
                onNameChange = { nameTextField = it },
                onSurnameChange = { surnameTextField = it },
                onBirthDateChange = { birthDateTextField = it },
                onNameFocus = { hasNameFocus = it },
                onSurnameFocus = { hasSurnameFocus = it },
                onBirthDateFocus = { hasBirthDateFocus = it },
                onBirthDateComplete = { localFocusManager.clearFocus() },
                dateFormat = dateFormat,
            )

            val pickerFieldColors = feedTrackerTextFieldColors()
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding, vertical = 8.dp)
                    .clickable { showDatePicker = true },
                value = formatCivilDate(sessionDate, dateFormat),
                onValueChange = {},
                enabled = false,
                colors = pickerFieldColors,
                label = { Text(stringResource(Res.string.past_session_date)) },
                shape = MaterialTheme.shapes.medium,
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding, vertical = 8.dp)
                    .clickable { showTimePicker = true },
                value = formatClockTime(startHour, startMinute),
                onValueChange = {},
                enabled = false,
                colors = pickerFieldColors,
                label = { Text(stringResource(Res.string.past_session_start_time)) },
                shape = MaterialTheme.shapes.medium,
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding, vertical = 8.dp),
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
                colors = feedTrackerTextFieldColors(),
                shape = MaterialTheme.shapes.medium,
            )

            if (!isPastSession) {
                Text(
                    text = stringResource(Res.string.past_session_must_be_in_the_past),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenHorizontalPadding, vertical = 4.dp),
                )
            }
        }

        Button(
            onClick = { viewModel.showPopup() },
            enabled = isButtonEnabled,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.BottomCenter),
        ) {
            Text(stringResource(Res.string.save_new_tracking))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = utcEpochMillisFromCivilDate(sessionDate),
            selectableDates = PastSelectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val shifted = startPartsKeepingSessionInPast(
                                date = civilDateFromUtcEpochMillis(millis),
                                hour = startHour,
                                minute = startMinute,
                                durationMs = durationMs,
                                nowMillis = getCurrentTimeMillis(),
                            )
                            sessionDate = shifted.date
                            startHour = shifted.hour
                            startMinute = shifted.minute
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = feedTrackerTextButtonColors(),
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startHour,
            initialMinute = startMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val shifted = startPartsKeepingSessionInPast(
                            date = sessionDate,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                            durationMs = durationMs,
                            nowMillis = getCurrentTimeMillis(),
                        )
                        sessionDate = shifted.date
                        startHour = shifted.hour
                        startMinute = shifted.minute
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false },
                    colors = feedTrackerTextButtonColors(),
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            },
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(Res.string.discard_session_title)) },
            text = { Text(stringResource(Res.string.discard_session_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        onBackButtonClick()
                    },
                ) {
                    Text(stringResource(Res.string.leave))
                }
            },
            dismissButton = {
                Button(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
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
                        viewModel.saveNewTracking(
                            name = nameTextField.text,
                            surname = surnameTextField.text,
                            birthDate = canonicalBirthDateFromDisplay(birthDateTextField.text, dateFormat)
                                ?: birthDateTextField.text,
                            additionalNotes = additionalNotesTextField.text,
                            startTime = startMillis,
                            endTime = startMillis + durationMs,
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

private object PastSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = isSelectablePastUtcDate(utcTimeMillis, getCurrentTimeMillis())
}

@Composable
private fun PersonNameFields(
    nameTextField: TextFieldValue,
    surnameTextField: TextFieldValue,
    birthDateTextField: TextFieldValue,
    hasNameFocus: Boolean,
    hasSurnameFocus: Boolean,
    hasBirthDateFocus: Boolean,
    onNameChange: (TextFieldValue) -> Unit,
    onSurnameChange: (TextFieldValue) -> Unit,
    onBirthDateChange: (TextFieldValue) -> Unit,
    onNameFocus: (Boolean) -> Unit,
    onSurnameFocus: (Boolean) -> Unit,
    onBirthDateFocus: (Boolean) -> Unit,
    onBirthDateComplete: () -> Unit,
    dateFormat: DateDisplayFormat,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding, vertical = 8.dp)
            .onFocusChanged { onNameFocus(it.hasFocus) },
        value = nameTextField,
        onValueChange = onNameChange,
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
                        .clickable { onNameChange(TextFieldValue("")) },
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Text,
            showKeyboardOnFocus = true,
            imeAction = ImeAction.Next,
        ),
        shape = MaterialTheme.shapes.medium,
        colors = feedTrackerTextFieldColors(),
    )
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding, vertical = 8.dp)
            .onFocusChanged { onSurnameFocus(it.hasFocus) },
        value = surnameTextField,
        onValueChange = onSurnameChange,
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
                        .clickable { onSurnameChange(TextFieldValue("")) },
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Text,
            showKeyboardOnFocus = true,
            imeAction = ImeAction.Next,
        ),
        shape = MaterialTheme.shapes.medium,
        colors = feedTrackerTextFieldColors(),
    )
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding, vertical = 8.dp)
            .onFocusChanged { onBirthDateFocus(it.hasFocus) },
        value = birthDateTextField,
        onValueChange = fun(input: TextFieldValue) {
            val change = birthDateChange(birthDateTextField.text, input.text, dateFormat) ?: return
            if (change.placeCursorAtEnd) {
                onBirthDateChange(input.copy(text = change.text, selection = TextRange(change.text.length)))
            } else {
                onBirthDateChange(input)
            }
            if (change.complete) onBirthDateComplete()
        },
        label = { Text(stringResource(Res.string.date_of_birth)) },
        placeholder = { Text(dateFormat.placeholder()) },
        singleLine = true,
        trailingIcon = {
            if (hasBirthDateFocus && birthDateTextField.text.isNotEmpty()) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.Clear),
                    contentDescription = stringResource(Res.string.clear_date_of_birth),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { onBirthDateChange(TextFieldValue("")) },
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            showKeyboardOnFocus = true,
            imeAction = ImeAction.Done,
        ),
        shape = MaterialTheme.shapes.medium,
        colors = feedTrackerTextFieldColors(),
    )
}
