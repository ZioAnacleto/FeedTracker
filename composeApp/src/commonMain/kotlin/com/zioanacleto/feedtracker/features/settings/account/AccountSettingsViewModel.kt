package com.zioanacleto.feedtracker.features.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import com.zioanacleto.feedtracker.domain.repositories.TrackingPreferencesRepository
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.features.settings.privacy.SessionExportSharer
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionController
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.unable_to_delete_account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

data class AccountSettingsUiState(
    val isLoggingOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val error: String? = null,
)

class AccountSettingsViewModel(
    private val authRepository: AuthRepository,
    private val authSessionRepository: AuthSessionRepository,
    private val sessionExportSharer: SessionExportSharer,
    private val trackingSessionsRepository: TrackingSessionsRepository,
    private val trackingPreferencesRepository: TrackingPreferencesRepository,
    private val activeTrackingSessionController: ActiveTrackingSessionController,
    private val stringResource: suspend (StringResource) -> String = { resource -> getString(resource) },
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    fun logout() {
        val current = _uiState.value
        if (current.isLoggingOut || current.isDeletingAccount) return
        val accessToken = authSessionRepository.session.value?.accessToken
        if (accessToken.isNullOrBlank()) {
            authSessionRepository.clearSession()
            viewModelScope.launch {
                runCatching { sessionExportSharer.clearCachedExports() }
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, error = null) }
            runCatching { authRepository.logout(accessToken) }
            authSessionRepository.clearSession()
            runCatching { sessionExportSharer.clearCachedExports() }
            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }

    fun deleteAccount(typedConfirmation: String, expectedConfirmation: String) {
        val current = _uiState.value
        if (current.isDeletingAccount || current.isLoggingOut) return
        if (typedConfirmation.trim() != expectedConfirmation.trim()) return
        val accessToken = authSessionRepository.session.value?.accessToken
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, error = null) }
            if (!accessToken.isNullOrBlank()) {
                val deleted = runCatching { authRepository.deleteAccount(accessToken) }
                if (deleted.isFailure) {
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            error = deleted.exceptionOrNull()?.message
                                ?: stringResource(Res.string.unable_to_delete_account),
                        )
                    }
                    return@launch
                }
            }
            clearLocalAccountData()
        }
    }

    private suspend fun clearLocalAccountData() {
        runCatching { trackingSessionsRepository.discardUnsyncedSessions() }
        runCatching { trackingPreferencesRepository.clearLocal() }
        runCatching { activeTrackingSessionController.clear() }
        runCatching { sessionExportSharer.clearCachedExports() }
        authSessionRepository.clearSession()
    }
}
