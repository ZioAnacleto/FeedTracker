package com.zioanacleto.feedtracker.common.models

import kotlinx.serialization.Serializable

@Serializable
data class HealthStatus(
    val status: String,
    val uptime: String,
)
