package com.zioanacleto.feedtracker.features.trackingpreferences.repositories

import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences

interface TrackingPreferencesRepository {
    suspend fun findByUserId(userId: String): TrackingPreferences?
    suspend fun upsert(userId: String, preferences: TrackingPreferences): TrackingPreferences
}
