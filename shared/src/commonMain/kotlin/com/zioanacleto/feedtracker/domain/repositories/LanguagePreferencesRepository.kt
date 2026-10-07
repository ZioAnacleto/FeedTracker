package com.zioanacleto.feedtracker.domain.repositories

import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import kotlinx.coroutines.flow.StateFlow

interface LanguagePreferencesRepository {
    val language: StateFlow<AppLanguage>

    fun setLanguage(language: AppLanguage)
}
