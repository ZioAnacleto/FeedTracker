package com.zioanacleto.feedtracker.domain

import kotlinx.serialization.Serializable

@Serializable
data class TrackingSessionModel(
    val id: String,
    val sessionStartTime: Long,
    val sessionEndTime: Long,
    val name: String,
    val surname: String,
    val birthDate: String, // format: DD/MM/YYYY
    val additionalNotes: String? = null
)