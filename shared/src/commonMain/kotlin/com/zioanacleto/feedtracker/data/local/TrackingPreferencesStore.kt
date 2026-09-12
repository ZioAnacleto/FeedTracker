package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import kotlinx.serialization.json.Json

interface TrackingPreferencesStore {
    fun load(): TrackingPreferences?
    fun save(preferences: TrackingPreferences)
}

class InMemoryTrackingPreferencesStore(initial: TrackingPreferences? = null) : TrackingPreferencesStore {
    private var preferences: TrackingPreferences? = initial

    override fun load(): TrackingPreferences? = preferences

    override fun save(preferences: TrackingPreferences) {
        this.preferences = preferences
    }
}

class JsonTrackingPreferencesStore(
    private val textStore: LocalTextStore,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : TrackingPreferencesStore {
    override fun load(): TrackingPreferences? {
        val text = textStore.read()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { json.decodeFromString(TrackingPreferences.serializer(), text) }.getOrNull()
    }

    override fun save(preferences: TrackingPreferences) {
        textStore.write(json.encodeToString(TrackingPreferences.serializer(), preferences))
    }
}

const val TRACKING_PREFERENCES_FILE_NAME = "tracking_preferences.json"
