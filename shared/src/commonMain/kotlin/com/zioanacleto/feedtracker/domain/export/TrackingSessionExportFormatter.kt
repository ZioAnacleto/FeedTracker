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
    append('\uFEFF')
    appendCsvLine(csvHeader.joinToString(","))
    sessions.forEach { session ->
        appendCsvLine(
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

private fun StringBuilder.appendCsvLine(line: String) {
    append(line)
    append("\r\n")
}

private fun csvField(value: String): String {
    val sanitized = sanitizeCsvInjection(value)
    val needsQuotes = sanitized.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
    if (!needsQuotes) return sanitized
    return "\"${sanitized.replace("\"", "\"\"")}\""
}

private fun sanitizeCsvInjection(value: String): String {
    if (value.isEmpty()) return value
    val first = value.first()
    if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
        return "'$value"
    }
    return value
}
