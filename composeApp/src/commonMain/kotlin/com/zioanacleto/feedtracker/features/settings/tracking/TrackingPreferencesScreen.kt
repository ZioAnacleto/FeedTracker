package com.zioanacleto.feedtracker.features.settings.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.components.birthDateChange
import com.zioanacleto.feedtracker.components.hideKeyboardOnTouch
import com.zioanacleto.feedtracker.components.placeholder
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.DurationDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.PersonPrefillMode
import com.zioanacleto.feedtracker.theme.ScreenHorizontalPadding
import com.zioanacleto.feedtracker.theme.feedTrackerScreenWindowInsets
import com.zioanacleto.feedtracker.theme.feedTrackerTextFieldColors
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.back
import feedtracker.composeapp.generated.resources.date_format
import feedtracker.composeapp.generated.resources.date_format_dmy
import feedtracker.composeapp.generated.resources.date_format_mdy
import feedtracker.composeapp.generated.resources.date_format_ymd
import feedtracker.composeapp.generated.resources.date_of_birth
import feedtracker.composeapp.generated.resources.day_start
import feedtracker.composeapp.generated.resources.day_start_hour
import feedtracker.composeapp.generated.resources.day_start_minute
import feedtracker.composeapp.generated.resources.default_person
import feedtracker.composeapp.generated.resources.duration_format_hours_minutes_option
import feedtracker.composeapp.generated.resources.duration_format_label
import feedtracker.composeapp.generated.resources.duration_format_minutes_seconds_option
import feedtracker.composeapp.generated.resources.name
import feedtracker.composeapp.generated.resources.person_prefill_custom
import feedtracker.composeapp.generated.resources.person_prefill_last_used
import feedtracker.composeapp.generated.resources.save_tracking_preferences
import feedtracker.composeapp.generated.resources.saving_tracking_preferences
import feedtracker.composeapp.generated.resources.surname
import feedtracker.composeapp.generated.resources.tracking_preferences
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackingPreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: TrackingPreferencesViewModel = koinViewModel(),
    onBackButtonClick: () -> Unit,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val savingDescription = stringResource(Res.string.saving_tracking_preferences)

    LaunchedEffect(uiState.saveSucceeded) {
        if (uiState.saveSucceeded) onSaved()
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
                    onClick = onBackButtonClick,
                    modifier = Modifier.align(Alignment.CenterStart),
                    enabled = !uiState.isSaving,
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                        contentDescription = stringResource(Res.string.back),
                    )
                }
                Text(
                    text = stringResource(Res.string.tracking_preferences),
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
                Text(stringResource(Res.string.date_format), style = MaterialTheme.typography.titleMedium)
                Column(modifier = Modifier.selectableGroup()) {
                    DateFormatOption(
                        label = stringResource(Res.string.date_format_dmy),
                        selected = uiState.dateFormat == DateDisplayFormat.DAY_MONTH_YEAR,
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.onDateFormatChange(DateDisplayFormat.DAY_MONTH_YEAR) },
                    )
                    DateFormatOption(
                        label = stringResource(Res.string.date_format_mdy),
                        selected = uiState.dateFormat == DateDisplayFormat.MONTH_DAY_YEAR,
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.onDateFormatChange(DateDisplayFormat.MONTH_DAY_YEAR) },
                    )
                    DateFormatOption(
                        label = stringResource(Res.string.date_format_ymd),
                        selected = uiState.dateFormat == DateDisplayFormat.YEAR_MONTH_DAY,
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.onDateFormatChange(DateDisplayFormat.YEAR_MONTH_DAY) },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(Res.string.day_start), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    NumberDropdown(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.day_start_hour),
                        value = uiState.dayStartHour,
                        range = 0..23,
                        enabled = !uiState.isSaving,
                        onValueChange = viewModel::onDayStartHourChange,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    NumberDropdown(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.day_start_minute),
                        value = uiState.dayStartMinute,
                        range = 0..59,
                        enabled = !uiState.isSaving,
                        onValueChange = viewModel::onDayStartMinuteChange,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(Res.string.duration_format_label), style = MaterialTheme.typography.titleMedium)
                Column(modifier = Modifier.selectableGroup()) {
                    DateFormatOption(
                        label = stringResource(Res.string.duration_format_minutes_seconds_option),
                        selected = uiState.durationFormat == DurationDisplayFormat.MINUTES_SECONDS,
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.onDurationFormatChange(DurationDisplayFormat.MINUTES_SECONDS) },
                    )
                    DateFormatOption(
                        label = stringResource(Res.string.duration_format_hours_minutes_option),
                        selected = uiState.durationFormat == DurationDisplayFormat.HOURS_MINUTES,
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.onDurationFormatChange(DurationDisplayFormat.HOURS_MINUTES) },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(Res.string.default_person), style = MaterialTheme.typography.titleMedium)
                Column(modifier = Modifier.selectableGroup()) {
                    DateFormatOption(
                        label = stringResource(Res.string.person_prefill_last_used),
                        selected = uiState.personPrefillMode == PersonPrefillMode.LAST_USED,
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.onPersonPrefillModeChange(PersonPrefillMode.LAST_USED) },
                    )
                    DateFormatOption(
                        label = stringResource(Res.string.person_prefill_custom),
                        selected = uiState.personPrefillMode == PersonPrefillMode.CUSTOM,
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.onPersonPrefillModeChange(PersonPrefillMode.CUSTOM) },
                    )
                }

                if (uiState.personPrefillMode == PersonPrefillMode.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.defaultPersonName,
                        onValueChange = viewModel::onDefaultPersonNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                        label = { Text(stringResource(Res.string.name)) },
                        singleLine = true,
                        colors = feedTrackerTextFieldColors(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.defaultPersonSurname,
                        onValueChange = viewModel::onDefaultPersonSurnameChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                        label = { Text(stringResource(Res.string.surname)) },
                        singleLine = true,
                        colors = feedTrackerTextFieldColors(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    var birthDateField by remember(uiState.defaultPersonBirthDate, uiState.dateFormat) {
                        mutableStateOf(TextFieldValue(uiState.defaultPersonBirthDate))
                    }
                    OutlinedTextField(
                        value = birthDateField,
                        onValueChange = { input ->
                            val change = birthDateChange(birthDateField.text, input.text, uiState.dateFormat) ?: return@OutlinedTextField
                            birthDateField = if (change.placeCursorAtEnd) {
                                input.copy(text = change.text, selection = TextRange(change.text.length))
                            } else {
                                input
                            }
                            viewModel.onDefaultPersonBirthDateChange(birthDateField.text)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                        label = { Text(stringResource(Res.string.date_of_birth)) },
                        placeholder = { Text(uiState.dateFormat.placeholder()) },
                        singleLine = true,
                        colors = feedTrackerTextFieldColors(),
                    )
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                ) {
                    Text(stringResource(Res.string.save_tracking_preferences))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .semantics { contentDescription = savingDescription },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun DateFormatOption(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberDropdown(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    range: IntRange,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = value.toString().padStart(2, '0')
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = feedTrackerTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            range.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString().padStart(2, '0')) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
