package com.zioanacleto.feedtracker.features.settings.privacy

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.zioanacleto.feedtracker.R
import java.io.File

class AndroidSessionExportSharer(private val context: Context) : SessionExportSharer {
    override val savesWithSystemPicker: Boolean = false

    override suspend fun shareTextFile(fileName: String, mimeType: String, content: String) {
        val exportDir = File(context.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeText(content)
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

    override suspend fun saveTextFile(fileName: String, mimeType: String, content: String) {
        error("Android saves exports through the system document picker")
    }

    private companion object {
        const val EXPORT_DIRECTORY = "exports"
    }
}
