package com.zioanacleto.feedtracker.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class AnonymizedTrackingSession(
    val initials: String,
    val birthDate: String,
    val sessionStartTime: String,
    val sessionEndTime: String,
    val additionalNotes: String? = null,
)
