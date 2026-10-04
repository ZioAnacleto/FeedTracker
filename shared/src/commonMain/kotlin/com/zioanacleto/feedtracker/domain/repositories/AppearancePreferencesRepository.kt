package com.zioanacleto.feedtracker.domain.repositories

import com.zioanacleto.feedtracker.domain.preferences.AppearanceMode
import kotlinx.coroutines.flow.StateFlow

interface AppearancePreferencesRepository {
    val mode: StateFlow<AppearanceMode>

    fun setMode(mode: AppearanceMode)
}
