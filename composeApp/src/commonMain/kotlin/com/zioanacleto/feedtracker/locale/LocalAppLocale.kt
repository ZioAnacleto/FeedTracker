package com.zioanacleto.feedtracker.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.zioanacleto.feedtracker.domain.preferences.languageTag
import com.zioanacleto.feedtracker.domain.repositories.LanguagePreferencesRepository
import org.koin.compose.koinInject

expect object LocalAppLocale {
    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

@Composable
fun ProvideAppLanguage(content: @Composable () -> Unit) {
    val repository = koinInject<LanguagePreferencesRepository>()
    val language by repository.language.collectAsState()
    CompositionLocalProvider(LocalAppLocale provides language.languageTag()) {
        key(language) {
            content()
        }
    }
}
