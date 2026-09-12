package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.domain.repositories.TrackingPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTrackingPreferencesRepository(initial: TrackingPreferences = TrackingPreferences.Default) : TrackingPreferencesRepository {
    private val _preferences = MutableStateFlow(initial)
    override val preferences: StateFlow<TrackingPreferences> = _preferences.asStateFlow()

    override suspend fun refreshFromRemote() = Unit

    override suspend fun save(preferences: TrackingPreferences) {
        _preferences.value = preferences
    }

    override suspend fun rememberLastUsedPerson(name: String, surname: String, birthDate: String) {
        _preferences.update {
            it.copy(
                lastUsedPersonName = name,
                lastUsedPersonSurname = surname,
                lastUsedPersonBirthDate = birthDate,
            )
        }
    }
}
