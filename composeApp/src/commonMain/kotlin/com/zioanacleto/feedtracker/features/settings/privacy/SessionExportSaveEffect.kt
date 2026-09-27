package com.zioanacleto.feedtracker.features.settings.privacy

import androidx.compose.runtime.Composable

@Composable
expect fun SessionExportSaveEffect(
    pendingSave: PendingSessionExport?,
    saveLauncherOpen: Boolean,
    onSaveLauncherOpened: () -> Unit,
    onSaved: () -> Unit,
    onCancelled: () -> Unit,
    onFailed: (Throwable) -> Unit,
    onCompleteSave: (suspend (PendingSessionExport) -> Unit) -> Unit,
)
