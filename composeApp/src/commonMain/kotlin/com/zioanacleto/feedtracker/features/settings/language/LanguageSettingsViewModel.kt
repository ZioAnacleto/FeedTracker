package com.zioanacleto.feedtracker.features.settings.language

import androidx.lifecycle.ViewModel
import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import com.zioanacleto.feedtracker.domain.repositories.LanguagePreferencesRepository
import kotlinx.coroutines.flow.StateFlow

class LanguageSettingsViewModel(private val languagePreferencesRepository: LanguagePreferencesRepository) : ViewModel() {
    val language: StateFlow<AppLanguage> = languagePreferencesRepository.language

    fun onLanguageChange(language: AppLanguage) {
        languagePreferencesRepository.setLanguage(language)
    }
}
