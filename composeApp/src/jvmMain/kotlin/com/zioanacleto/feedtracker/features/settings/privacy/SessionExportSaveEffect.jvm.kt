package com.zioanacleto.feedtracker.features.settings.privacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun SessionExportSaveEffect(
    pendingSave: PendingSessionExport?,
    saveLauncherOpen: Boolean,
    onSaveLauncherOpened: () -> Unit,
    onSaved: () -> Unit,
    onCancelled: () -> Unit,
    onFailed: (Throwable) -> Unit,
    onCompleteSave: (suspend (PendingSessionExport) -> Unit) -> Unit,
) {
    LaunchedEffect(pendingSave, saveLauncherOpen) {
        val request = pendingSave ?: return@LaunchedEffect
        if (saveLauncherOpen) return@LaunchedEffect
        onSaveLauncherOpened()
        runCatching {
            JvmSessionExportSharer().saveTextFile(request.fileName, request.mimeType, request.content)
        }.onSuccess { onSaved() }.onFailure(onFailed)
    }
}
