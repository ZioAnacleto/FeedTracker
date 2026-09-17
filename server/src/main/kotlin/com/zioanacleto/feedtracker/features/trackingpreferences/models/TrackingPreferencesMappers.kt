package com.zioanacleto.feedtracker.features.trackingpreferences.models

import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.DurationDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.PersonPrefillMode
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

fun ResultRow.toTrackingPreferences(): TrackingPreferences = TrackingPreferences(
    dateFormat = DateDisplayFormat.valueOf(this[TrackingPreferencesTable.dateFormat]),
    dayStartHour = this[TrackingPreferencesTable.dayStartHour],
    dayStartMinute = this[TrackingPreferencesTable.dayStartMinute],
    durationFormat = DurationDisplayFormat.valueOf(this[TrackingPreferencesTable.durationFormat]),
    personPrefillMode = PersonPrefillMode.valueOf(this[TrackingPreferencesTable.personPrefillMode]),
    defaultPersonName = this[TrackingPreferencesTable.defaultPersonName],
    defaultPersonSurname = this[TrackingPreferencesTable.defaultPersonSurname],
    defaultPersonBirthDate = this[TrackingPreferencesTable.defaultPersonBirthDate],
    lastUsedPersonName = this[TrackingPreferencesTable.lastUsedPersonName],
    lastUsedPersonSurname = this[TrackingPreferencesTable.lastUsedPersonSurname],
    lastUsedPersonBirthDate = this[TrackingPreferencesTable.lastUsedPersonBirthDate],
)

fun UpdateBuilder<*>.writeTrackingPreferences(preferences: TrackingPreferences) {
    this[TrackingPreferencesTable.dateFormat] = preferences.dateFormat.name
    this[TrackingPreferencesTable.dayStartHour] = preferences.dayStartHour
    this[TrackingPreferencesTable.dayStartMinute] = preferences.dayStartMinute
    this[TrackingPreferencesTable.durationFormat] = preferences.durationFormat.name
    this[TrackingPreferencesTable.personPrefillMode] = preferences.personPrefillMode.name
    this[TrackingPreferencesTable.defaultPersonName] = preferences.defaultPersonName
    this[TrackingPreferencesTable.defaultPersonSurname] = preferences.defaultPersonSurname
    this[TrackingPreferencesTable.defaultPersonBirthDate] = preferences.defaultPersonBirthDate
    this[TrackingPreferencesTable.lastUsedPersonName] = preferences.lastUsedPersonName
    this[TrackingPreferencesTable.lastUsedPersonSurname] = preferences.lastUsedPersonSurname
    this[TrackingPreferencesTable.lastUsedPersonBirthDate] = preferences.lastUsedPersonBirthDate
}
