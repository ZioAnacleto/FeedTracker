package com.zioanacleto.feedtracker.features.settings.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonalSettingsUiState(val isLoggingOut: Boolean = false)

class PersonalSettingsViewModel(private val authRepository: AuthRepository, private val authSessionRepository: AuthSessionRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(PersonalSettingsUiState())
    val uiState: StateFlow<PersonalSettingsUiState> = _uiState.asStateFlow()

    fun logout() {
        if (_uiState.value.isLoggingOut) return
        val accessToken = authSessionRepository.session.value?.accessToken
        if (accessToken.isNullOrBlank()) {
            authSessionRepository.clearSession()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            runCatching { authRepository.logout(accessToken) }
            authSessionRepository.clearSession()
            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }
}
