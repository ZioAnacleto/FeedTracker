package com.zioanacleto.feedtracker.domain

import kotlinx.serialization.Serializable

@Serializable
data class CreateTrackingSessionRequest(
    val sessionStartTime: Long,
    val sessionEndTime: Long,
    val name: String,
    val surname: String,
    val birthDate: String,
    val additionalNotes: String? = null,
)
