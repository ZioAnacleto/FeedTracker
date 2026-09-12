package com.zioanacleto.feedtracker.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.unable_to_reset_password
import feedtracker.composeapp.generated.resources.unable_to_send_verification
import feedtracker.composeapp.generated.resources.unable_to_verify_code
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

enum class ForgotPasswordStep {
    Email,
    Code,
    Password,
}

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.Email,
    val email: String = "",
    val code: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val resetToken: String? = null,
)

class ForgotPasswordViewModel(private val authRepository: AuthRepository, private val authSessionRepository: AuthSessionRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onCodeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(CODE_LENGTH)
        _uiState.update { it.copy(code = digits, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun goBack(): Boolean {
        val current = _uiState.value
        if (current.isSubmitting) return true
        return when (current.step) {
            ForgotPasswordStep.Email -> false
            ForgotPasswordStep.Code -> {
                _uiState.update { it.copy(step = ForgotPasswordStep.Email, code = "", error = null) }
                true
            }
            ForgotPasswordStep.Password -> {
                _uiState.update { it.copy(step = ForgotPasswordStep.Code, error = null) }
                true
            }
        }
    }

    fun sendResetCode() {
        val current = _uiState.value
        if (current.isSubmitting || current.email.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            runCatching {
                authRepository.startPasswordReset(current.email)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        step = ForgotPasswordStep.Code,
                        error = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = throwable.message ?: getString(Res.string.unable_to_send_verification),
                    )
                }
            }
        }
    }

    fun verifyCode() {
        val current = _uiState.value
        if (current.isSubmitting || current.code.length != CODE_LENGTH) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            runCatching {
                authRepository.verifyPasswordResetCode(current.email, current.code)
            }.onSuccess { token ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        step = ForgotPasswordStep.Password,
                        resetToken = token,
                        error = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = throwable.message ?: getString(Res.string.unable_to_verify_code),
                    )
                }
            }
        }
    }

    fun resetPassword() {
        val current = _uiState.value
        val token = current.resetToken
        if (current.isSubmitting || token.isNullOrBlank() || current.password.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            runCatching {
                authRepository.resetPassword(token, current.password)
            }.onSuccess { session ->
                authSessionRepository.setSession(session)
                _uiState.update { it.copy(isSubmitting = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = throwable.message ?: getString(Res.string.unable_to_reset_password),
                    )
                }
            }
        }
    }

    companion object {
        const val CODE_LENGTH = 6
    }
}
