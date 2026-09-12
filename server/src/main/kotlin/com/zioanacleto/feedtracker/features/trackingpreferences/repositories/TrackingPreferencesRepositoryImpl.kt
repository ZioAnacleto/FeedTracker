package com.zioanacleto.feedtracker.features.trackingpreferences.repositories

import com.zioanacleto.feedtracker.common.utils.transactionDb
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.features.trackingpreferences.models.TrackingPreferencesTable
import com.zioanacleto.feedtracker.features.trackingpreferences.models.toTrackingPreferences
import com.zioanacleto.feedtracker.features.trackingpreferences.models.writeTrackingPreferences
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class TrackingPreferencesRepositoryImpl : TrackingPreferencesRepository {
    override suspend fun findByUserId(userId: String): TrackingPreferences? = transactionDb {
        TrackingPreferencesTable
            .selectAll()
            .where { TrackingPreferencesTable.userId eq userId }
            .singleOrNull()
            ?.toTrackingPreferences()
    }

    override suspend fun upsert(userId: String, preferences: TrackingPreferences): TrackingPreferences = transactionDb {
        val exists = TrackingPreferencesTable
            .selectAll()
            .where { TrackingPreferencesTable.userId eq userId }
            .any()
        if (exists) {
            TrackingPreferencesTable.update({ TrackingPreferencesTable.userId eq userId }) {
                it.writeTrackingPreferences(preferences)
            }
        } else {
            TrackingPreferencesTable.insert {
                it[TrackingPreferencesTable.userId] = userId
                it.writeTrackingPreferences(preferences)
            }
        }
        preferences
    }
}
