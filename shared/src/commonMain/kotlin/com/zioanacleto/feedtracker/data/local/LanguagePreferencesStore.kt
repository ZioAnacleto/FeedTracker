package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface LanguagePreferencesStore {
    fun load(): AppLanguage

    fun save(language: AppLanguage)
}

class InMemoryLanguagePreferencesStore(initial: AppLanguage = AppLanguage.SYSTEM) : LanguagePreferencesStore {
    private var language: AppLanguage = initial

    override fun load(): AppLanguage = language

    override fun save(language: AppLanguage) {
        this.language = language
    }
}

@Serializable
private data class LanguagePreferences(val language: AppLanguage = AppLanguage.SYSTEM)

class JsonLanguagePreferencesStore(
    private val textStore: LocalTextStore,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : LanguagePreferencesStore {
    override fun load(): AppLanguage {
        val text = textStore.read()?.takeIf { it.isNotBlank() } ?: return AppLanguage.SYSTEM
        return runCatching {
            json.decodeFromString(LanguagePreferences.serializer(), text).language
        }.getOrDefault(AppLanguage.SYSTEM)
    }

    override fun save(language: AppLanguage) {
        textStore.write(json.encodeToString(LanguagePreferences.serializer(), LanguagePreferences(language)))
    }
}

const val LANGUAGE_PREFERENCES_FILE_NAME = "language_preferences.json"
