package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.local.AppearancePreferencesStore
import com.zioanacleto.feedtracker.domain.preferences.AppearanceMode
import com.zioanacleto.feedtracker.domain.repositories.AppearancePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppearancePreferencesRepositoryImpl(private val store: AppearancePreferencesStore) : AppearancePreferencesRepository {
    private val _mode = MutableStateFlow(store.load())
    override val mode: StateFlow<AppearanceMode> = _mode.asStateFlow()

    override fun setMode(mode: AppearanceMode) {
        store.save(mode)
        _mode.value = mode
    }
}
