package com.zioanacleto.feedtracker.features.settings.privacy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.components.formatBirthDateForDisplay
import com.zioanacleto.feedtracker.components.formatSessionDateTime
import com.zioanacleto.feedtracker.domain.export.ExportRequiresConnectionException
import com.zioanacleto.feedtracker.domain.export.anonymizeTrackingSessions
import com.zioanacleto.feedtracker.domain.export.formatTrackingSessionsCsv
import com.zioanacleto.feedtracker.domain.export.formatTrackingSessionsJson
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.repositories.TrackingPreferencesRepository
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.export_sessions_requires_connection
import feedtracker.composeapp.generated.resources.unable_to_export_sessions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

data class PrivacySettingsUiState(
    val isExporting: Boolean = false,
    val error: String? = null,
    val pendingSave: PendingSessionExport? = null,
    val saveLauncherOpen: Boolean = false,
    val saveSucceeded: Boolean = false,
    val showsSeparateSaveActions: Boolean = true,
)

class PrivacySettingsViewModel(
    private val trackingSessionsRepository: TrackingSessionsRepository,
    private val trackingPreferencesRepository: TrackingPreferencesRepository,
    private val sessionExportSharer: SessionExportSharer,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val formatBirthDate: (String, DateDisplayFormat) -> String = { value, format ->
        formatBirthDateForDisplay(value, format)
    },
    private val formatDateTime: (Long, DateDisplayFormat) -> String = { millis, format ->
        formatSessionDateTime(millis, format)
    },
) : ViewModel() {
    private val _uiState = MutableStateFlow(restoredState())
    val uiState: StateFlow<PrivacySettingsUiState> = _uiState.asStateFlow()

    fun exportSessions(format: SessionExportFormat, destination: SessionExportDestination) {
        if (_uiState.value.isExporting) return
        viewModelScope.launch {
            updateUiState {
                it.copy(
                    isExporting = true,
                    error = null,
                    saveSucceeded = false,
                    pendingSave = null,
                    saveLauncherOpen = false,
                )
            }
            try {
                val sessions = trackingSessionsRepository.getAllTrackingSessionsForExport()
                val dateFormat = trackingPreferencesRepository.preferences.value.dateFormat
                val anonymized = anonymizeTrackingSessions(
                    sessions = sessions,
                    formatBirthDate = { formatBirthDate(it, dateFormat) },
                    formatDateTime = { formatDateTime(it, dateFormat) },
                )
                val content = when (format) {
                    SessionExportFormat.CSV -> formatTrackingSessionsCsv(anonymized)
                    SessionExportFormat.JSON -> formatTrackingSessionsJson(anonymized)
                }
                when (destination) {
                    SessionExportDestination.SHARE -> {
                        sessionExportSharer.shareTextFile(format.fileName, format.mimeType, content)
                        updateUiState { it.copy(isExporting = false) }
                    }
                    SessionExportDestination.SAVE -> {
                        if (sessionExportSharer.requiresComposeSaveLauncher) {
                            updateUiState {
                                it.copy(
                                    isExporting = false,
                                    pendingSave = PendingSessionExport(format.fileName, format.mimeType, content),
                                    saveLauncherOpen = false,
                                )
                            }
                        } else {
                            val result = sessionExportSharer.saveTextFile(format.fileName, format.mimeType, content)
                            updateUiState {
                                it.copy(
                                    isExporting = false,
                                    saveSucceeded = result == SessionExportSaveResult.SAVED,
                                )
                            }
                        }
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                val fallback = when (throwable) {
                    is ExportRequiresConnectionException -> Res.string.export_sessions_requires_connection
                    else -> Res.string.unable_to_export_sessions
                }
                updateUiState {
                    it.copy(
                        isExporting = false,
                        pendingSave = null,
                        saveLauncherOpen = false,
                        error = throwable.message ?: getString(fallback),
                    )
                }
            }
        }
    }

    fun onSaveLauncherOpened() {
        updateUiState { it.copy(saveLauncherOpen = true) }
    }

    fun completePendingSave(write: suspend (PendingSessionExport) -> Unit) {
        val request = _uiState.value.pendingSave
        viewModelScope.launch {
            if (request == null) {
                onSaveFailed(IllegalStateException("Unable to save the export file"))
                return@launch
            }
            try {
                write(request)
                onSaveCompleted()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                onSaveFailed(throwable)
            }
        }
    }

    fun onSaveCompleted() {
        updateUiState { it.copy(pendingSave = null, saveLauncherOpen = false, saveSucceeded = true, error = null) }
    }

    fun onSaveCancelled() {
        updateUiState { it.copy(pendingSave = null, saveLauncherOpen = false) }
    }

    fun onSaveFailed(throwable: Throwable) {
        viewModelScope.launch {
            updateUiState {
                it.copy(
                    pendingSave = null,
                    saveLauncherOpen = false,
                    error = throwable.message ?: getString(Res.string.unable_to_export_sessions),
                )
            }
        }
    }

    fun onSaveSucceededMessageShown() {
        updateUiState { it.copy(saveSucceeded = false) }
    }

    private fun restoredState(): PrivacySettingsUiState {
        val fileName = savedStateHandle.get<String>(KEY_FILE_NAME)
        val mimeType = savedStateHandle.get<String>(KEY_MIME_TYPE)
        val content = savedStateHandle.get<String>(KEY_CONTENT)
        val pendingSave = if (fileName != null && mimeType != null && content != null) {
            PendingSessionExport(fileName, mimeType, content)
        } else {
            null
        }
        return PrivacySettingsUiState(
            showsSeparateSaveActions = sessionExportSharer.showsSeparateSaveActions,
            pendingSave = pendingSave,
            saveLauncherOpen = pendingSave != null && savedStateHandle.get<Boolean>(KEY_LAUNCHER_OPEN) == true,
        )
    }

    private inline fun updateUiState(transform: (PrivacySettingsUiState) -> PrivacySettingsUiState) {
        val next = _uiState.updateAndGet(transform)
        persistSaveRequest(next.pendingSave, next.saveLauncherOpen)
    }

    private fun persistSaveRequest(pendingSave: PendingSessionExport?, saveLauncherOpen: Boolean) {
        if (pendingSave == null) {
            savedStateHandle.remove<String>(KEY_FILE_NAME)
            savedStateHandle.remove<String>(KEY_MIME_TYPE)
            savedStateHandle.remove<String>(KEY_CONTENT)
            savedStateHandle[KEY_LAUNCHER_OPEN] = false
            return
        }
        savedStateHandle[KEY_FILE_NAME] = pendingSave.fileName
        savedStateHandle[KEY_MIME_TYPE] = pendingSave.mimeType
        savedStateHandle[KEY_CONTENT] = pendingSave.content
        savedStateHandle[KEY_LAUNCHER_OPEN] = saveLauncherOpen
    }

    private companion object {
        const val KEY_FILE_NAME = "pending_save_file_name"
        const val KEY_MIME_TYPE = "pending_save_mime_type"
        const val KEY_CONTENT = "pending_save_content"
        const val KEY_LAUNCHER_OPEN = "save_launcher_open"
    }
}
