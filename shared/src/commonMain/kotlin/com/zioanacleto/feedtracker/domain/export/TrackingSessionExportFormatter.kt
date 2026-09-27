package com.zioanacleto.feedtracker.domain.export

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val exportJson = Json {
    prettyPrint = true
    encodeDefaults = true
}

private val csvHeader = listOf(
    "initials",
    "birthDate",
    "sessionStartTime",
    "sessionEndTime",
    "additionalNotes",
)

fun formatTrackingSessionsCsv(sessions: List<AnonymizedTrackingSession>): String = buildString {
    appendLine(csvHeader.joinToString(","))
    sessions.forEach { session ->
        appendLine(
            listOf(
                csvField(session.initials),
                csvField(session.birthDate),
                csvField(session.sessionStartTime),
                csvField(session.sessionEndTime),
                csvField(session.additionalNotes.orEmpty()),
            ).joinToString(","),
        )
    }
}

fun formatTrackingSessionsJson(sessions: List<AnonymizedTrackingSession>): String =
    exportJson.encodeToString(ListSerializer(AnonymizedTrackingSession.serializer()), sessions)

private fun csvField(value: String): String {
    val needsQuotes = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
    if (!needsQuotes) return value
    return "\"${value.replace("\"", "\"\"")}\""
}
