package com.zioanacleto.feedtracker.features.settings.privacy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val currentPendingSave by rememberUpdatedState(pendingSave)
    val currentOnCancelled by rememberUpdatedState(onCancelled)
    val currentOnFailed by rememberUpdatedState(onFailed)
    val currentOnCompleteSave by rememberUpdatedState(onCompleteSave)
    val launcher = rememberLauncherForActivityResult(CreateNamedDocument()) { uri ->
        if (uri == null) {
            currentOnCancelled()
            return@rememberLauncherForActivityResult
        }
        val request = currentPendingSave
        if (request == null) {
            scope.launch {
                withContext(Dispatchers.IO) { deleteCreatedDocument(context, uri) }
                currentOnFailed(IllegalStateException("Unable to save the export file"))
            }
            return@rememberLauncherForActivityResult
        }
        currentOnCompleteSave { pending ->
            try {
                withContext(Dispatchers.IO) { writeExport(context, uri, pending.content) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                withContext(Dispatchers.IO) { deleteCreatedDocument(context, uri) }
                throw throwable
            }
        }
    }

    LaunchedEffect(pendingSave, saveLauncherOpen) {
        val request = pendingSave ?: return@LaunchedEffect
        if (saveLauncherOpen) return@LaunchedEffect
        onSaveLauncherOpened()
        launcher.launch(request)
    }
}

private class CreateNamedDocument : ActivityResultContract<PendingSessionExport, Uri?>() {
    override fun createIntent(context: Context, input: PendingSessionExport): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = input.mimeType
        putExtra(Intent.EXTRA_TITLE, input.fileName)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data.takeIf { resultCode == Activity.RESULT_OK }
}

private fun writeExport(context: Context, uri: Uri, content: String) {
    val output = context.contentResolver.openOutputStream(uri)
        ?: error("Unable to save the export file")
    output.use { stream ->
        stream.write(content.encodeToByteArray())
        stream.flush()
    }
}

private fun deleteCreatedDocument(context: Context, uri: Uri) {
    runCatching { DocumentsContract.deleteDocument(context.contentResolver, uri) }
}
