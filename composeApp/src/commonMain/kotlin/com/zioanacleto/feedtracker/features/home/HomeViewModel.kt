package com.zioanacleto.feedtracker.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.unable_to_delete_tracking_session
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val items: List<HomeSessionListItem>,
        val selectedSession: TrackingSessionModel? = null,
        val isDeleting: Boolean = false,
        val deleteError: String? = null,
        val undoableDeletedSession: TrackingSessionModel? = null,
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val trackingSessionsRepository: TrackingSessionsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var sessions: List<TrackingSessionModel> = emptyList()
    private var expandedGroupKeys: Set<String> = emptySet()
    private var pendingDeletedSession: TrackingSessionModel? = null
    private var pendingDeleteJob: Job? = null

    fun loadSessions() {
        viewModelScope.launch {
            trackingSessionsRepository.getTrackingSessions().collect { resource ->
                _uiState.value = when (resource) {
                    Resource.Loading -> HomeUiState.Loading
                    is Resource.Success -> {
                        sessions = resource.data.filterNot { it.id == pendingDeletedSession?.id }
                        readyState(keepSelection = true)
                    }
                    is Resource.Error -> HomeUiState.Error(resource.message)
                }
            }
        }
    }

    fun toggleGroup(key: String) {
        if (_uiState.value !is HomeUiState.Ready) return
        expandedGroupKeys = if (key in expandedGroupKeys) {
            expandedGroupKeys - key
        } else {
            expandedGroupKeys + key
        }
        _uiState.value = readyState(keepSelection = true)
    }

    fun selectSession(session: TrackingSessionModel) {
        if (_uiState.value !is HomeUiState.Ready) return
        _uiState.value = readyState(
            selectedSession = session,
            deleteError = null,
        )
    }

    fun dismissSessionDetail() {
        if (_uiState.value !is HomeUiState.Ready) return
        _uiState.value = readyState(selectedSession = null, deleteError = null)
    }

    fun deleteSelectedSession() {
        val current = _uiState.value as? HomeUiState.Ready ?: return
        val session = current.selectedSession ?: return
        viewModelScope.launch {
            val previousDelayJob = pendingDeleteJob
            pendingDeleteJob = null
            commitPendingDelete()
            previousDelayJob?.cancel()

            pendingDeletedSession = session
            sessions = sessions.filterNot { it.id == session.id }
            _uiState.value = readyState(selectedSession = null)
            pendingDeleteJob = launch {
                delay(DELETE_UNDO_WINDOW_MS)
                commitPendingDelete()
            }
        }
    }

    fun undoDelete() {
        val session = pendingDeletedSession ?: return
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        pendingDeletedSession = null
        if (sessions.none { it.id == session.id }) {
            sessions = sessions + session
        }
        _uiState.value = readyState(selectedSession = null)
    }

    private suspend fun commitPendingDelete() {
        val session = pendingDeletedSession ?: return
        pendingDeletedSession = null
        pendingDeleteJob = null
        try {
            withContext(NonCancellable) {
                trackingSessionsRepository.deleteTrackingSession(session.id)
            }
            if (_uiState.value is HomeUiState.Ready) {
                _uiState.value = readyState(selectedSession = null)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            if (sessions.none { it.id == session.id }) {
                sessions = sessions + session
            }
            if (_uiState.value is HomeUiState.Ready) {
                _uiState.value = readyState(
                    selectedSession = null,
                    deleteError = throwable.message
                        ?: getString(Res.string.unable_to_delete_tracking_session),
                )
            }
        }
    }

    private fun readyState(
        selectedSession: TrackingSessionModel? = (_uiState.value as? HomeUiState.Ready)?.selectedSession,
        isDeleting: Boolean = false,
        deleteError: String? = null,
        keepSelection: Boolean = false,
    ): HomeUiState.Ready {
        val resolvedSelection = if (keepSelection) {
            selectedSession?.takeIf { selected -> sessions.any { it.id == selected.id } }
        } else {
            selectedSession
        }
        return HomeUiState.Ready(
            items = buildHomeSessionListItems(sessions, expandedGroupKeys),
            selectedSession = resolvedSelection,
            isDeleting = isDeleting,
            deleteError = deleteError,
            undoableDeletedSession = pendingDeletedSession,
        )
    }

    companion object {
        const val DELETE_UNDO_WINDOW_MS = 5_000L
    }
}
