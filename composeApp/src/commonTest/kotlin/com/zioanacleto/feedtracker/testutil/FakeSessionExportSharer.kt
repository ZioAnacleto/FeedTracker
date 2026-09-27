package com.zioanacleto.feedtracker.testutil

import com.zioanacleto.feedtracker.features.settings.privacy.SessionExportSaveResult
import com.zioanacleto.feedtracker.features.settings.privacy.SessionExportSharer
import kotlinx.coroutines.delay

class FakeSessionExportSharer(
    private val shareError: Throwable? = null,
    private val saveError: Throwable? = null,
    private val saveResult: SessionExportSaveResult = SessionExportSaveResult.SAVED,
    private val shareDelayMillis: Long = 0,
    override val requiresComposeSaveLauncher: Boolean = false,
    override val showsSeparateSaveActions: Boolean = true,
) : SessionExportSharer {
    val shared = mutableListOf<SharedTextFile>()
    val saved = mutableListOf<SharedTextFile>()

    override suspend fun shareTextFile(fileName: String, mimeType: String, content: String) {
        if (shareDelayMillis > 0) {
            delay(shareDelayMillis)
        }
        shareError?.let { throw it }
        shared += SharedTextFile(fileName, mimeType, content)
    }

    override suspend fun saveTextFile(fileName: String, mimeType: String, content: String): SessionExportSaveResult {
        saveError?.let { throw it }
        if (saveResult == SessionExportSaveResult.SAVED) {
            saved += SharedTextFile(fileName, mimeType, content)
        }
        return saveResult
    }
}

data class SharedTextFile(val fileName: String, val mimeType: String, val content: String)
