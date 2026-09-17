package com.zioanacleto.feedtracker.features.newtracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.data.repositories.InMemoryTrackingPreferencesRepository
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.repositories.TrackingPreferencesRepository
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.unable_to_save_tracking_session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface SaveTrackingUiState {
    data object Idle : SaveTrackingUiState
    data object Saving : SaveTrackingUiState
    data object Saved : SaveTrackingUiState
    data class Error(val message: String) : SaveTrackingUiState
}

@OptIn(ExperimentalUuidApi::class)
class NewTrackingViewModel(
    private val trackingSessionsRepository: TrackingSessionsRepository,
    private val trackingPreferencesRepository: TrackingPreferencesRepository = InMemoryTrackingPreferencesRepository(),
) : ViewModel() {

    private val _showPopup = MutableStateFlow(false)
    val showPopup: StateFlow<Boolean> = _showPopup.asStateFlow()

    private val _saveState = MutableStateFlow<SaveTrackingUiState>(SaveTrackingUiState.Idle)
    val saveState: StateFlow<SaveTrackingUiState> = _saveState.asStateFlow()

    fun saveNewTracking(
        name: String,
        surname: String,
        birthDate: String,
        additionalNotes: String? = null,
        startTime: Long,
        endTime: Long,
    ) {
        val trackingModel = TrackingSessionModel(
            id = Uuid.random().toString(),
            name = name,
            surname = surname,
            birthDate = birthDate,
            additionalNotes = additionalNotes?.takeIf { it.isNotBlank() },
            sessionStartTime = startTime,
            sessionEndTime = endTime,
        )

        viewModelScope.launch {
            _saveState.value = SaveTrackingUiState.Saving
            runCatching {
                trackingSessionsRepository.saveTrackingSession(trackingModel)
            }.onSuccess {
                trackingPreferencesRepository.rememberLastUsedPerson(name, surname, birthDate)
                _saveState.value = SaveTrackingUiState.Saved
            }.onFailure { throwable ->
                _saveState.value = SaveTrackingUiState.Error(
                    throwable.message ?: getString(Res.string.unable_to_save_tracking_session),
                )
            }
        }
    }

    fun showPopup() {
        _showPopup.value = true
    }

    fun hidePopup() {
        _showPopup.value = false
    }

    fun consumeSaveState() {
        _saveState.value = SaveTrackingUiState.Idle
    }
}
