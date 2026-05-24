package com.zioanacleto.feedtracker.domain

data class TrackingSessionModel(
    val id: String, // todo: generate id
    val sessionStartTime: Long,
    val sessionEndTime: Long,
    val name: String,
    val surname: String,
    val birthDate: String, // format: DD/MM/YYYY
    val additionalNotes: String? = null
)