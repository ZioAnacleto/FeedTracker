package com.zioanacleto.feedtracker.features.settings.privacy

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

internal class DocumentExportPickerSession {
    private var onResult: ((SessionExportSaveResult) -> Unit)? = null

    val isOpen: Boolean
        get() = onResult != null

    fun open(onResult: (SessionExportSaveResult) -> Unit) {
        check(!isOpen) { "An export picker is already open" }
        this.onResult = onResult
    }

    fun onDocumentsPicked() {
        finish(SessionExportSaveResult.SAVED)
    }

    fun onCancelled() {
        finish(SessionExportSaveResult.CANCELLED)
    }

    fun abandon() {
        onResult = null
    }

    private fun finish(result: SessionExportSaveResult) {
        val callback = onResult ?: return
        onResult = null
        callback(result)
    }
}

internal suspend fun awaitDocumentExport(
    session: DocumentExportPickerSession,
    onCancel: () -> Unit = {},
    present: () -> Unit,
): SessionExportSaveResult = suspendCancellableCoroutine { continuation ->
    session.open { result ->
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }
    continuation.invokeOnCancellation {
        session.abandon()
        onCancel()
    }
    try {
        present()
    } catch (throwable: Throwable) {
        session.abandon()
        if (throwable is CancellationException) throw throwable
        if (continuation.isActive) {
            continuation.resumeWith(Result.failure(throwable))
        }
    }
}

internal fun exportWriteFailureMessage(written: Boolean, platformMessage: String?): String? {
    if (written) return null
    return platformMessage?.takeIf { it.isNotBlank() } ?: "Unable to write the export file"
}
