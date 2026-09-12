package com.zioanacleto.feedtracker.domain.repositories

import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import kotlinx.coroutines.flow.StateFlow

interface TrackingPreferencesRepository {
    val preferences: StateFlow<TrackingPreferences>

    suspend fun refreshFromRemote()

    suspend fun save(preferences: TrackingPreferences)

    suspend fun rememberLastUsedPerson(name: String, surname: String, birthDate: String)
}
