package com.zioanacleto.feedtracker.domain.export

import com.zioanacleto.feedtracker.domain.TrackingSessionModel

fun anonymizeTrackingSessions(
    sessions: List<TrackingSessionModel>,
    formatBirthDate: (String) -> String = { it },
    formatDateTime: (Long) -> String = { it.toString() },
): List<AnonymizedTrackingSession> {
    val initialsByPerson = uniqueInitialsByPerson(sessions)
    return sessions.map { session ->
        AnonymizedTrackingSession(
            initials = initialsByPerson.getValue(personKey(session)),
            birthDate = formatBirthDate(session.birthDate),
            sessionStartTime = formatDateTime(session.sessionStartTime),
            sessionEndTime = formatDateTime(session.sessionEndTime),
            additionalNotes = session.additionalNotes,
        )
    }
}

internal fun personKey(session: TrackingSessionModel): String =
    "${session.name.trim().lowercase()}\u0000${session.surname.trim().lowercase()}"

internal fun personInitials(name: String, surname: String): String {
    val first = name.trim().firstOrNull()?.uppercaseChar()
    val last = surname.trim().firstOrNull()?.uppercaseChar()
    return buildString {
        if (first != null) {
            append(first)
            append('.')
        }
        if (last != null) {
            append(last)
            append('.')
        }
        if (isEmpty()) {
            append("?.")
        }
    }
}

private fun uniqueInitialsByPerson(sessions: List<TrackingSessionModel>): Map<String, String> {
    val people = sessions
        .distinctBy(::personKey)
        .sortedWith(compareBy(::personKey).thenBy { it.id })
    val usedCounts = mutableMapOf<String, Int>()
    return people.associate { session ->
        val base = personInitials(session.name, session.surname)
        val count = (usedCounts[base] ?: 0) + 1
        usedCounts[base] = count
        val unique = if (count == 1) base else "$base$count"
        personKey(session) to unique
    }
}
