package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.domain.preferences.AppearanceMode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface AppearancePreferencesStore {
    fun load(): AppearanceMode

    fun save(mode: AppearanceMode)
}

class InMemoryAppearancePreferencesStore(initial: AppearanceMode = AppearanceMode.SYSTEM) : AppearancePreferencesStore {
    private var mode: AppearanceMode = initial

    override fun load(): AppearanceMode = mode

    override fun save(mode: AppearanceMode) {
        this.mode = mode
    }
}

@Serializable
private data class AppearancePreferences(val mode: AppearanceMode = AppearanceMode.SYSTEM)

class JsonAppearancePreferencesStore(
    private val textStore: LocalTextStore,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : AppearancePreferencesStore {
    override fun load(): AppearanceMode {
        val text = textStore.read()?.takeIf { it.isNotBlank() } ?: return AppearanceMode.SYSTEM
        return runCatching {
            json.decodeFromString(AppearancePreferences.serializer(), text).mode
        }.getOrDefault(AppearanceMode.SYSTEM)
    }

    override fun save(mode: AppearanceMode) {
        textStore.write(json.encodeToString(AppearancePreferences.serializer(), AppearancePreferences(mode)))
    }
}

const val APPEARANCE_PREFERENCES_FILE_NAME = "appearance_preferences.json"
