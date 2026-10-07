package com.zioanacleto.feedtracker.domain.preferences

import kotlinx.serialization.Serializable

/**
 * Language of the app UI.
 *
 * Date and duration formats stay in tracking preferences. Those patterns are numeric
 * (`dd/MM/yyyy`, minutes and seconds) and are not derived from this choice.
 */
@Serializable
enum class AppLanguage {
    SYSTEM,
    ENGLISH,
    ITALIAN,
}

fun AppLanguage.languageTag(): String? = when (this) {
    AppLanguage.SYSTEM -> null
    AppLanguage.ENGLISH -> "en"
    AppLanguage.ITALIAN -> "it"
}

fun appLanguageFromTag(tag: String?): AppLanguage = when (tag) {
    "en" -> AppLanguage.ENGLISH
    "it" -> AppLanguage.ITALIAN
    else -> AppLanguage.SYSTEM
}
