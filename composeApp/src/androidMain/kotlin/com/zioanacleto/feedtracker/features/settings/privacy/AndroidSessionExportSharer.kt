package com.zioanacleto.feedtracker.features.settings.privacy

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.zioanacleto.feedtracker.R
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File

class AndroidSessionExportSharer(private val context: Context, private val dispatcherProvider: DispatcherProvider) : SessionExportSharer {
    override val requiresComposeSaveLauncher: Boolean = true

    override suspend fun shareTextFile(fileName: String, mimeType: String, content: String) {
        val file = withContext(dispatcherProvider.io()) {
            writeCachedExport(context.cacheDir, fileName, content)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            clipData = ClipData.newRawUri(fileName, uri)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, context.getString(R.string.export_sessions_share_title))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    override suspend fun clearCachedExports() {
        withContext(dispatcherProvider.io()) {
            deleteCachedExports(context.cacheDir)
        }
    }
}

internal fun writeCachedExport(cacheDir: File, fileName: String, content: String): File {
    val exportDir = File(cacheDir, EXPORT_DIRECTORY)
    exportDir.deleteRecursively()
    exportDir.mkdirs()
    return File(exportDir, fileName).apply { writeText(content) }
}

internal fun deleteCachedExports(cacheDir: File) {
    File(cacheDir, EXPORT_DIRECTORY).deleteRecursively()
}

private const val EXPORT_DIRECTORY = "exports"
