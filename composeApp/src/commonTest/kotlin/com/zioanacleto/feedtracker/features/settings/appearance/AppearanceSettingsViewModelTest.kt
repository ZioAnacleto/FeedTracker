package com.zioanacleto.feedtracker.features.settings.appearance

import com.zioanacleto.feedtracker.data.local.InMemoryAppearancePreferencesStore
import com.zioanacleto.feedtracker.data.repositories.AppearancePreferencesRepositoryImpl
import com.zioanacleto.feedtracker.domain.preferences.AppearanceMode
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AppearanceSettingsViewModelTest {

    @Test
    fun selectingAModePersistsItImmediately() {
        val repository = AppearancePreferencesRepositoryImpl(InMemoryAppearancePreferencesStore())
        val viewModel = AppearanceSettingsViewModel(repository)

        viewModel.mode.value shouldBe AppearanceMode.SYSTEM

        viewModel.onModeChange(AppearanceMode.LIGHT)

        viewModel.mode.value shouldBe AppearanceMode.LIGHT
        repository.mode.value shouldBe AppearanceMode.LIGHT
    }
}
