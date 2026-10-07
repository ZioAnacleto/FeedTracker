package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.local.LanguagePreferencesStore
import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import com.zioanacleto.feedtracker.domain.repositories.LanguagePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LanguagePreferencesRepositoryImpl(private val store: LanguagePreferencesStore) : LanguagePreferencesRepository {
    private val _language = MutableStateFlow(store.load())
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        store.save(language)
        _language.value = language
    }
}
