package com.zioanacleto.feedtracker.features.settings.tracking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
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
import feedtracker.composeapp.generated.resources.date_format_description
import feedtracker.composeapp.generated.resources.date_format_dmy
import feedtracker.composeapp.generated.resources.date_format_mdy
import feedtracker.composeapp.generated.resources.date_format_ymd
import feedtracker.composeapp.generated.resources.date_of_birth
import feedtracker.composeapp.generated.resources.day_start
import feedtracker.composeapp.generated.resources.day_start_description
import feedtracker.composeapp.generated.resources.day_start_hour
import feedtracker.composeapp.generated.resources.day_start_minute
import feedtracker.composeapp.generated.resources.default_person
import feedtracker.composeapp.generated.resources.default_person_description
import feedtracker.composeapp.generated.resources.duration_format_description
import feedtracker.composeapp.generated.resources.duration_format_hours_minutes_example
import feedtracker.composeapp.generated.resources.duration_format_hours_minutes_option
import feedtracker.composeapp.generated.resources.duration_format_label
import feedtracker.composeapp.generated.resources.duration_format_minutes_seconds_example
import feedtracker.composeapp.generated.resources.duration_format_minutes_seconds_option
import feedtracker.composeapp.generated.resources.name
import feedtracker.composeapp.generated.resources.person_prefill_custom
import feedtracker.composeapp.generated.resources.person_prefill_custom_description
import feedtracker.composeapp.generated.resources.person_prefill_last_used
import feedtracker.composeapp.generated.resources.person_prefill_last_used_description
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
                    .padding(horizontal = ScreenHorizontalPadding)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PreferenceSection(
                    title = stringResource(Res.string.date_format),
                    description = stringResource(Res.string.date_format_description),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompactChoice(
                            modifier = Modifier.weight(1f),
                            label = stringResource(Res.string.date_format_dmy),
                            selected = uiState.dateFormat == DateDisplayFormat.DAY_MONTH_YEAR,
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.onDateFormatChange(DateDisplayFormat.DAY_MONTH_YEAR) },
                        )
                        CompactChoice(
                            modifier = Modifier.weight(1f),
                            label = stringResource(Res.string.date_format_mdy),
                            selected = uiState.dateFormat == DateDisplayFormat.MONTH_DAY_YEAR,
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.onDateFormatChange(DateDisplayFormat.MONTH_DAY_YEAR) },
                        )
                        CompactChoice(
                            modifier = Modifier.weight(1f),
                            label = stringResource(Res.string.date_format_ymd),
                            selected = uiState.dateFormat == DateDisplayFormat.YEAR_MONTH_DAY,
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.onDateFormatChange(DateDisplayFormat.YEAR_MONTH_DAY) },
                        )
                    }
                }

                PreferenceSection(
                    title = stringResource(Res.string.day_start),
                    description = stringResource(Res.string.day_start_description),
                ) {
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
                }

                PreferenceSection(
                    title = stringResource(Res.string.duration_format_label),
                    description = stringResource(Res.string.duration_format_description),
                ) {
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DetailedChoice(
                            title = stringResource(Res.string.duration_format_minutes_seconds_option),
                            subtitle = stringResource(Res.string.duration_format_minutes_seconds_example),
                            selected = uiState.durationFormat == DurationDisplayFormat.MINUTES_SECONDS,
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.onDurationFormatChange(DurationDisplayFormat.MINUTES_SECONDS) },
                        )
                        DetailedChoice(
                            title = stringResource(Res.string.duration_format_hours_minutes_option),
                            subtitle = stringResource(Res.string.duration_format_hours_minutes_example),
                            selected = uiState.durationFormat == DurationDisplayFormat.HOURS_MINUTES,
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.onDurationFormatChange(DurationDisplayFormat.HOURS_MINUTES) },
                        )
                    }
                }

                PreferenceSection(
                    title = stringResource(Res.string.default_person),
                    description = stringResource(Res.string.default_person_description),
                ) {
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DetailedChoice(
                            title = stringResource(Res.string.person_prefill_last_used),
                            subtitle = stringResource(Res.string.person_prefill_last_used_description),
                            selected = uiState.personPrefillMode == PersonPrefillMode.LAST_USED,
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.onPersonPrefillModeChange(PersonPrefillMode.LAST_USED) },
                        )
                        DetailedChoice(
                            title = stringResource(Res.string.person_prefill_custom),
                            subtitle = stringResource(Res.string.person_prefill_custom_description),
                            selected = uiState.personPrefillMode == PersonPrefillMode.CUSTOM,
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.onPersonPrefillModeChange(PersonPrefillMode.CUSTOM) },
                        )
                    }

                    AnimatedVisibility(visible = uiState.personPrefillMode == PersonPrefillMode.CUSTOM) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
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
                    }
                }

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                ) {
                    Text(stringResource(Res.string.save_tracking_preferences))
                }
                Spacer(modifier = Modifier.height(8.dp))
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
private fun PreferenceSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CompactChoice(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.selectable(
            selected = selected,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        shape = MaterialTheme.shapes.small,
        color = if (selected) colors.surfaceContainerHighest else colors.surface.copy(alpha = 0.28f),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.outline else colors.outline.copy(alpha = 0.35f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = colors.onSurface.copy(alpha = if (enabled) 1f else 0.5f),
        )
    }
}

@Composable
private fun DetailedChoice(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.small,
        color = if (selected) colors.surfaceContainerHighest else colors.surface.copy(alpha = 0.28f),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.outline else colors.outline.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface.copy(alpha = if (enabled) 1f else 0.5f),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = if (enabled) 0.75f else 0.4f),
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.outline,
                )
            }
        }
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
