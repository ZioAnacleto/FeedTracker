package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.local.TrackingPreferencesStore
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import com.zioanacleto.feedtracker.domain.repositories.TrackingPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrackingPreferencesRepositoryImpl(
    private val store: TrackingPreferencesStore,
    private val authRepository: AuthRepository,
    private val authSessionRepository: AuthSessionRepository,
) : TrackingPreferencesRepository {
    private val _preferences = MutableStateFlow(store.load() ?: TrackingPreferences.Default)
    override val preferences: StateFlow<TrackingPreferences> = _preferences.asStateFlow()

    override suspend fun refreshFromRemote() {
        val accessToken = authSessionRepository.session.value?.accessToken ?: return
        runCatching { authRepository.getTrackingPreferences(accessToken) }.onSuccess { remote ->
            persist(remote)
        }
    }

    override suspend fun save(preferences: TrackingPreferences) {
        persist(preferences)
        val accessToken = authSessionRepository.session.value?.accessToken ?: return
        val remote = authRepository.updateTrackingPreferences(accessToken, preferences)
        persist(remote)
    }

    override suspend fun rememberLastUsedPerson(name: String, surname: String, birthDate: String) {
        val updated = _preferences.value.copy(
            lastUsedPersonName = name,
            lastUsedPersonSurname = surname,
            lastUsedPersonBirthDate = birthDate,
        )
        persist(updated)
        val accessToken = authSessionRepository.session.value?.accessToken ?: return
        runCatching { authRepository.updateTrackingPreferences(accessToken, updated) }.onSuccess { remote ->
            persist(remote)
        }
    }

    private fun persist(preferences: TrackingPreferences) {
        store.save(preferences)
        _preferences.value = preferences
    }
}
