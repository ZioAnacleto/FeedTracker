package com.zioanacleto.feedtracker.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.unable_to_load_auth_methods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

sealed interface LoginMethodsUiState {
    data object Loading : LoginMethodsUiState
    data class Ready(val methods: List<AuthMethod>, val showUnsupportedMethod: Boolean = false) : LoginMethodsUiState
    data class Error(val message: String) : LoginMethodsUiState
}

class LoginMethodsViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginMethodsUiState>(LoginMethodsUiState.Loading)
    val uiState: StateFlow<LoginMethodsUiState> = _uiState.asStateFlow()

    init {
        loadMethods()
    }

    fun loadMethods() {
        viewModelScope.launch {
            _uiState.value = LoginMethodsUiState.Loading
            runCatching { authRepository.getAvailableAuthMethods() }
                .onSuccess { methods ->
                    _uiState.value = LoginMethodsUiState.Ready(methods)
                }
                .onFailure { throwable ->
                    _uiState.value = LoginMethodsUiState.Error(
                        throwable.message ?: getString(Res.string.unable_to_load_auth_methods),
                    )
                }
        }
    }

    fun onUnsupportedMethod() {
        val current = _uiState.value as? LoginMethodsUiState.Ready ?: return
        _uiState.value = current.copy(showUnsupportedMethod = true)
    }
}
