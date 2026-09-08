package com.zioanacleto.feedtracker.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.unable_to_login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

data class EmailLoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
)

class EmailLoginViewModel(private val authRepository: AuthRepository, private val authSessionRepository: AuthSessionRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(EmailLoginUiState())
    val uiState: StateFlow<EmailLoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val current = _uiState.value
        if (current.isLoggingIn || current.email.isBlank() || current.password.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingIn = true, error = null) }
            runCatching {
                authRepository.loginWithEmail(current.email, current.password)
            }.onSuccess { session ->
                authSessionRepository.setSession(session)
                _uiState.update { it.copy(isLoggingIn = false, loggedIn = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoggingIn = false,
                        error = throwable.message ?: getString(Res.string.unable_to_login),
                    )
                }
            }
        }
    }
}
