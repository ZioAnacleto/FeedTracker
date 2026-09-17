package com.zioanacleto.feedtracker.features.trackingpreferences.models

import org.jetbrains.exposed.sql.Table

object TrackingPreferencesTable : Table("user_tracking_preferences") {
    val userId = varchar("user_id", 36)
    val dateFormat = varchar("date_format", 32)
    val dayStartHour = integer("day_start_hour")
    val dayStartMinute = integer("day_start_minute")
    val durationFormat = varchar("duration_format", 32)
    val personPrefillMode = varchar("person_prefill_mode", 32)
    val defaultPersonName = varchar("default_person_name", 255)
    val defaultPersonSurname = varchar("default_person_surname", 255)
    val defaultPersonBirthDate = varchar("default_person_birth_date", 10)
    val lastUsedPersonName = varchar("last_used_person_name", 255)
    val lastUsedPersonSurname = varchar("last_used_person_surname", 255)
    val lastUsedPersonBirthDate = varchar("last_used_person_birth_date", 10)

    override val primaryKey = PrimaryKey(userId)
}
