package com.zioanacleto.feedtracker.features.newtracking.navigation

import kotlinx.serialization.Serializable

@Serializable
data class NewTrackingRoute(val name: String = "", val surname: String = "", val birthDate: String = "")
