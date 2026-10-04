package com.zioanacleto.feedtracker.features.settings.appearance

import androidx.lifecycle.ViewModel
import com.zioanacleto.feedtracker.domain.preferences.AppearanceMode
import com.zioanacleto.feedtracker.domain.repositories.AppearancePreferencesRepository
import kotlinx.coroutines.flow.StateFlow

class AppearanceSettingsViewModel(private val appearancePreferencesRepository: AppearancePreferencesRepository) : ViewModel() {
    val mode: StateFlow<AppearanceMode> = appearancePreferencesRepository.mode

    fun onModeChange(mode: AppearanceMode) {
        appearancePreferencesRepository.setMode(mode)
    }
}
