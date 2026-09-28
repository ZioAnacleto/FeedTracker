package com.zioanacleto.feedtracker.features.settings.privacy

enum class SessionExportFormat(val fileName: String, val mimeType: String) {
    CSV("feedtracker-sessions.csv", "text/csv"),
    JSON("feedtracker-sessions.json", "application/json"),
}

enum class SessionExportDestination {
    SHARE,
    SAVE,
}

data class PendingSessionExport(val fileName: String, val mimeType: String, val content: String)

enum class SessionExportSaveResult {
    SAVED,
    CANCELLED,
}

interface SessionExportSharer {
    val requiresComposeSaveLauncher: Boolean get() = false
    val showsSeparateSaveActions: Boolean get() = true

    suspend fun shareTextFile(fileName: String, mimeType: String, content: String)

    suspend fun clearCachedExports() {}

    suspend fun saveTextFile(fileName: String, mimeType: String, content: String): SessionExportSaveResult =
        error("This platform saves exports through the Compose document launcher")
}
