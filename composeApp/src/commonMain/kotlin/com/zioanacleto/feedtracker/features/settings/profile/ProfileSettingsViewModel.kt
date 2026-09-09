package com.zioanacleto.feedtracker.features.settings.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.unable_to_update_profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

data class ProfileSettingsUiState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val error: String? = null,
)

class ProfileSettingsViewModel(private val authRepository: AuthRepository, private val authSessionRepository: AuthSessionRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<ProfileSettingsUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, error = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, error = null) }
    }

    fun saveProfile() {
        val current = _uiState.value
        val session = authSessionRepository.session.value
        if (current.isSaving ||
            current.saveSucceeded ||
            session == null ||
            current.firstName.isBlank() ||
            current.lastName.isBlank()
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching {
                authRepository.updateProfile(session.accessToken, current.firstName, current.lastName)
            }.onSuccess { user ->
                authSessionRepository.setSession(session.copy(user = user))
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSucceeded = true,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: getString(Res.string.unable_to_update_profile),
                    )
                }
            }
        }
    }

    private fun initialState(): ProfileSettingsUiState {
        val user = authSessionRepository.session.value?.user
        return ProfileSettingsUiState(
            email = user?.email.orEmpty(),
            firstName = user?.firstName.orEmpty(),
            lastName = user?.lastName.orEmpty(),
        )
    }
}
