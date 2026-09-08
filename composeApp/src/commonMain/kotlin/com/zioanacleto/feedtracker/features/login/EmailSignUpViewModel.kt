package com.zioanacleto.feedtracker.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.unable_to_complete_registration
import feedtracker.composeapp.generated.resources.unable_to_send_verification
import feedtracker.composeapp.generated.resources.unable_to_verify_code
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

enum class EmailSignUpStep {
    Email,
    Code,
    Profile,
}

data class EmailSignUpUiState(
    val step: EmailSignUpStep = EmailSignUpStep.Email,
    val email: String = "",
    val code: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val registrationToken: String? = null,
)

class EmailSignUpViewModel(private val authRepository: AuthRepository, private val authSessionRepository: AuthSessionRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(EmailSignUpUiState())
    val uiState: StateFlow<EmailSignUpUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onCodeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(CODE_LENGTH)
        _uiState.update { it.copy(code = digits, error = null) }
    }

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, error = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun goBack(): Boolean {
        val current = _uiState.value
        if (current.isSubmitting) return true
        return when (current.step) {
            EmailSignUpStep.Email -> false
            EmailSignUpStep.Code -> {
                _uiState.update { it.copy(step = EmailSignUpStep.Email, code = "", error = null) }
                true
            }
            EmailSignUpStep.Profile -> {
                _uiState.update { it.copy(step = EmailSignUpStep.Code, error = null) }
                true
            }
        }
    }

    fun sendVerificationCode() {
        val current = _uiState.value
        if (current.isSubmitting || current.email.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            runCatching {
                authRepository.startEmailRegistration(current.email)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        step = EmailSignUpStep.Code,
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
                authRepository.verifyEmailCode(current.email, current.code)
            }.onSuccess { token ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        step = EmailSignUpStep.Profile,
                        registrationToken = token,
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

    fun completeRegistration() {
        val current = _uiState.value
        val token = current.registrationToken
        if (current.isSubmitting ||
            token.isNullOrBlank() ||
            current.firstName.isBlank() ||
            current.lastName.isBlank() ||
            current.password.isBlank()
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            runCatching {
                authRepository.completeEmailRegistration(
                    registrationToken = token,
                    password = current.password,
                    firstName = current.firstName,
                    lastName = current.lastName,
                )
            }.onSuccess { session ->
                authSessionRepository.setSession(session)
                _uiState.update { it.copy(isSubmitting = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = throwable.message ?: getString(Res.string.unable_to_complete_registration),
                    )
                }
            }
        }
    }

    companion object {
        const val CODE_LENGTH = 6
    }
}
