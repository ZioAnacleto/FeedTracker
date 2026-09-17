package com.zioanacleto.feedtracker.features.settings.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.components.canonicalBirthDateFromDisplay
import com.zioanacleto.feedtracker.components.formatBirthDateForDisplay
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.DurationDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.PersonPrefillMode
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.domain.repositories.TrackingPreferencesRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.invalid_birth_date_format
import feedtracker.composeapp.generated.resources.unable_to_save_tracking_preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

data class TrackingPreferencesUiState(
    val dateFormat: DateDisplayFormat = DateDisplayFormat.DAY_MONTH_YEAR,
    val dayStartHour: Int = 0,
    val dayStartMinute: Int = 0,
    val durationFormat: DurationDisplayFormat = DurationDisplayFormat.MINUTES_SECONDS,
    val personPrefillMode: PersonPrefillMode = PersonPrefillMode.LAST_USED,
    val defaultPersonName: String = "",
    val defaultPersonSurname: String = "",
    val defaultPersonBirthDate: String = "",
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val error: String? = null,
)

class TrackingPreferencesViewModel(private val trackingPreferencesRepository: TrackingPreferencesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<TrackingPreferencesUiState> = _uiState.asStateFlow()

    fun onDateFormatChange(value: DateDisplayFormat) {
        _uiState.update { current ->
            val canonical = canonicalBirthDateFromDisplay(current.defaultPersonBirthDate, current.dateFormat)
                ?: current.defaultPersonBirthDate
            current.copy(
                dateFormat = value,
                defaultPersonBirthDate = formatBirthDateForDisplay(canonical, value),
                error = null,
            )
        }
    }

    fun onDayStartHourChange(value: Int) {
        _uiState.update { it.copy(dayStartHour = value.coerceIn(0, 23), error = null) }
    }

    fun onDayStartMinuteChange(value: Int) {
        _uiState.update { it.copy(dayStartMinute = value.coerceIn(0, 59), error = null) }
    }

    fun onDurationFormatChange(value: DurationDisplayFormat) {
        _uiState.update { it.copy(durationFormat = value, error = null) }
    }

    fun onPersonPrefillModeChange(value: PersonPrefillMode) {
        _uiState.update { it.copy(personPrefillMode = value, error = null) }
    }

    fun onDefaultPersonNameChange(value: String) {
        _uiState.update { it.copy(defaultPersonName = value, error = null) }
    }

    fun onDefaultPersonSurnameChange(value: String) {
        _uiState.update { it.copy(defaultPersonSurname = value, error = null) }
    }

    fun onDefaultPersonBirthDateChange(value: String) {
        _uiState.update { it.copy(defaultPersonBirthDate = value, error = null) }
    }

    fun save() {
        val current = _uiState.value
        if (current.isSaving || current.saveSucceeded) return
        viewModelScope.launch {
            val canonicalBirthDate = current.defaultPersonBirthDate.takeIf { it.isNotBlank() }?.let { display ->
                canonicalBirthDateFromDisplay(display, current.dateFormat)
            }.orEmpty()
            if (current.defaultPersonBirthDate.isNotBlank() && canonicalBirthDate.isBlank()) {
                _uiState.update { it.copy(error = getString(Res.string.invalid_birth_date_format)) }
                return@launch
            }
            _uiState.update { it.copy(isSaving = true, error = null) }
            val stored = trackingPreferencesRepository.preferences.value
            val preferences = stored.copy(
                dateFormat = current.dateFormat,
                dayStartHour = current.dayStartHour,
                dayStartMinute = current.dayStartMinute,
                durationFormat = current.durationFormat,
                personPrefillMode = current.personPrefillMode,
                defaultPersonName = current.defaultPersonName.trim(),
                defaultPersonSurname = current.defaultPersonSurname.trim(),
                defaultPersonBirthDate = canonicalBirthDate,
            )
            runCatching {
                trackingPreferencesRepository.save(preferences)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saveSucceeded = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: getString(Res.string.unable_to_save_tracking_preferences),
                    )
                }
            }
        }
    }

    private fun initialState(): TrackingPreferencesUiState {
        val preferences = trackingPreferencesRepository.preferences.value
        return TrackingPreferencesUiState(
            dateFormat = preferences.dateFormat,
            dayStartHour = preferences.dayStartHour,
            dayStartMinute = preferences.dayStartMinute,
            durationFormat = preferences.durationFormat,
            personPrefillMode = preferences.personPrefillMode,
            defaultPersonName = preferences.defaultPersonName,
            defaultPersonSurname = preferences.defaultPersonSurname,
            defaultPersonBirthDate = formatBirthDateForDisplay(
                preferences.defaultPersonBirthDate,
                preferences.dateFormat,
            ),
        )
    }
}
