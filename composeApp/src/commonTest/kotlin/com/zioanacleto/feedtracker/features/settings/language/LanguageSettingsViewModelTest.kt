package com.zioanacleto.feedtracker.features.settings.language

import com.zioanacleto.feedtracker.data.local.InMemoryLanguagePreferencesStore
import com.zioanacleto.feedtracker.data.repositories.LanguagePreferencesRepositoryImpl
import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LanguageSettingsViewModelTest {

    @Test
    fun selectingALanguagePersistsItImmediately() {
        val repository = LanguagePreferencesRepositoryImpl(InMemoryLanguagePreferencesStore())
        val viewModel = LanguageSettingsViewModel(repository)

        viewModel.language.value shouldBe AppLanguage.SYSTEM

        viewModel.onLanguageChange(AppLanguage.ITALIAN)

        viewModel.language.value shouldBe AppLanguage.ITALIAN
        repository.language.value shouldBe AppLanguage.ITALIAN
    }
}
