package com.zioanacleto.feedtracker.features.pasttracking.navigation

import kotlinx.serialization.Serializable

@Serializable
data class PastTrackingRoute(val name: String = "", val surname: String = "", val birthDate: String = "")
