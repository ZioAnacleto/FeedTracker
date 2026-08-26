package com.zioanacleto.feedtracker.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val sessions: List<TrackingSessionModel>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val trackingSessionsRepository: TrackingSessionsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadSessions() {
        viewModelScope.launch {
            trackingSessionsRepository.getTrackingSessions().collect { resource ->
                _uiState.value = when (resource) {
                    Resource.Loading -> HomeUiState.Loading
                    is Resource.Success -> HomeUiState.Ready(resource.data)
                    is Resource.Error -> HomeUiState.Error(resource.message)
                }
            }
        }
    }
}
