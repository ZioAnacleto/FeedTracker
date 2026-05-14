package com.zioanacleto.feedtracker.domain

data class TrackingSessionModel(
    val id: String,
    val sessionStartTime: Long,
    val sessionEndTime: Long,
    val name: String,
    val surname: String,
    val birthDate: String, // format: YYYY-mm-DD
    val additionalNotes: String? = null
)