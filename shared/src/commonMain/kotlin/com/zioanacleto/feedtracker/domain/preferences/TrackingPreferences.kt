package com.zioanacleto.feedtracker.domain.preferences

import kotlinx.serialization.Serializable

@Serializable
enum class DateDisplayFormat {
    DAY_MONTH_YEAR,
    MONTH_DAY_YEAR,
    YEAR_MONTH_DAY,
}

@Serializable
enum class DurationDisplayFormat {
    MINUTES_SECONDS,
    HOURS_MINUTES,
}

@Serializable
enum class PersonPrefillMode {
    LAST_USED,
    CUSTOM,
}

@Serializable
data class TrackingPreferences(
    val dateFormat: DateDisplayFormat = DateDisplayFormat.DAY_MONTH_YEAR,
    val dayStartHour: Int = 0,
    val dayStartMinute: Int = 0,
    val durationFormat: DurationDisplayFormat = DurationDisplayFormat.MINUTES_SECONDS,
    val personPrefillMode: PersonPrefillMode = PersonPrefillMode.LAST_USED,
    val defaultPersonName: String = "",
    val defaultPersonSurname: String = "",
    val defaultPersonBirthDate: String = "",
    val lastUsedPersonName: String = "",
    val lastUsedPersonSurname: String = "",
    val lastUsedPersonBirthDate: String = "",
) {
    fun prefillPerson(): PersonIdentity = when (personPrefillMode) {
        PersonPrefillMode.LAST_USED -> PersonIdentity(
            name = lastUsedPersonName,
            surname = lastUsedPersonSurname,
            birthDate = lastUsedPersonBirthDate,
        )
        PersonPrefillMode.CUSTOM -> PersonIdentity(
            name = defaultPersonName,
            surname = defaultPersonSurname,
            birthDate = defaultPersonBirthDate,
        )
    }

    companion object {
        val Default = TrackingPreferences()
    }
}

data class PersonIdentity(val name: String = "", val surname: String = "", val birthDate: String = "") {
    fun orFallback(fallback: PersonIdentity): PersonIdentity =
        if (name.isNotBlank() || surname.isNotBlank() || birthDate.isNotBlank()) this else fallback
}
