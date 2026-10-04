package com.zioanacleto.feedtracker.domain.preferences

import kotlinx.serialization.Serializable

@Serializable
enum class AppearanceMode {
    SYSTEM,
    DARK,
    LIGHT,
}

fun AppearanceMode.isDarkTheme(systemDark: Boolean): Boolean = when (this) {
    AppearanceMode.SYSTEM -> systemDark
    AppearanceMode.DARK -> true
    AppearanceMode.LIGHT -> false
}
